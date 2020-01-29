package com.sequenceiq.it.cloudbreak.testcase.e2e.sdx;

import static com.sequenceiq.it.cloudbreak.context.RunningParameter.key;
import static java.lang.String.format;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.testng.annotations.Test;

import com.sequenceiq.cloudbreak.api.endpoint.v4.stacks.base.InstanceStatus;
import com.sequenceiq.it.cloudbreak.client.ImageCatalogTestClient;
import com.sequenceiq.it.cloudbreak.client.SdxTestClient;
import com.sequenceiq.it.cloudbreak.cloud.HostGroupType;
import com.sequenceiq.it.cloudbreak.cloud.v4.aws.AwsCloudProvider;
import com.sequenceiq.it.cloudbreak.context.Description;
import com.sequenceiq.it.cloudbreak.context.TestContext;
import com.sequenceiq.it.cloudbreak.dto.ClouderaManagerTestDto;
import com.sequenceiq.it.cloudbreak.dto.ClusterTestDto;
import com.sequenceiq.it.cloudbreak.dto.ImageSettingsTestDto;
import com.sequenceiq.it.cloudbreak.dto.imagecatalog.ImageCatalogTestDto;
import com.sequenceiq.it.cloudbreak.dto.sdx.SdxInternalTestDto;
import com.sequenceiq.it.cloudbreak.dto.stack.StackTestDto;
import com.sequenceiq.it.cloudbreak.exception.TestFailException;
import com.sequenceiq.it.cloudbreak.log.Log;
import com.sequenceiq.it.cloudbreak.testcase.e2e.AbstractE2ETest;
import com.sequenceiq.it.cloudbreak.util.wait.WaitUtil;
import com.sequenceiq.sdx.api.model.SdxClusterStatusResponse;

public class SdxUpgradeTests extends AbstractE2ETest {
    private static final Logger LOGGER = LoggerFactory.getLogger(SdxUpgradeTests.class);

    private Map<String, InstanceStatus> instancesHealthy = new HashMap<>() {{
        put(HostGroupType.MASTER.getName(), InstanceStatus.SERVICES_HEALTHY);
        put(HostGroupType.IDBROKER.getName(), InstanceStatus.SERVICES_HEALTHY);
    }};

    @Inject
    private ImageCatalogTestClient imageCatalogTestClient;

    @Inject
    private SdxTestClient sdxTestClient;

    @Inject
    private AwsCloudProvider awsCloudProvider;

    @Inject
    private WaitUtil waitUtil;

    @Value("sdx-upgrade-test-catalog")
    private String customImageCatalogName;

    @Value("https://cb-group.s3.eu-central-1.amazonaws.com/test/imagecatalog/sdx-upgrade-test-catalog.json")
    private String customImageCatalogUrl;

    @Value("9a72c4a6-fe05-4b41-62f3-cc0a1ed35df4")
    private String customImageId;

    @Value("redhat7")
    private String customOsType;

    @Override
    protected void setupTest(TestContext testContext) {
        createDefaultUser(testContext);
        createDefaultCredential(testContext);
        createEnvironmentForSdx(testContext);
        initializeDefaultBlueprints(testContext);
    }

    @Test(dataProvider = TEST_CONTEXT)
    @Description(
            given = "there is a running Cloudbreak, and an SDX cluster in available state",
            when = "a newer AWS image is available with same package versions for SDX",
            then = "image upgrade should be successful and SDX should be in RUNNING state again"
    )
    public void testSDXCanBeUpgradedSuccessfully(TestContext testContext) {
        String sdxInternal = resourcePropertyProvider().getName();
        String cluster = resourcePropertyProvider().getName();
        String clouderaManager = resourcePropertyProvider().getName();
        String imageSettings = resourcePropertyProvider().getName();
        String stack = resourcePropertyProvider().getName();

        testContext
                .given(customImageCatalogName, ImageCatalogTestDto.class).withName(customImageCatalogName).withUrl(customImageCatalogUrl)
                .when(imageCatalogTestClient.createV4(), key(customImageCatalogName))
                .given(imageSettings, ImageSettingsTestDto.class)
                .withName(imageSettings).withImageCatalog(customImageCatalogName).withImageId(customImageId).withOs(customOsType)
                .given(clouderaManager, ClouderaManagerTestDto.class)
                .given(cluster, ClusterTestDto.class).withClouderaManager(clouderaManager)
                .given(stack, StackTestDto.class).withCluster(cluster).withImageSettings(imageSettings)
                .given(sdxInternal, SdxInternalTestDto.class).withStackRequest(stack, cluster)
                .when(sdxTestClient.createInternal(), key(sdxInternal))
                .await(SdxClusterStatusResponse.RUNNING, key(sdxInternal))
                .then((tc, testDto, client) -> {
                    return waitUtil.waitForSdxInstancesStatus(testDto, client, getSdxInstancesHealthyState());
                })
                .when((tc, testDto, client) -> {
                    return sdxTestClient.checkForUpgrade().action(tc, testDto, client);
                })
                .when((tc, testDto, client) -> {
                    return sdxTestClient.upgrade().action(tc, testDto, client);
                })
                .await(SdxClusterStatusResponse.UPGRADE_IN_PROGRESS, key(sdxInternal))
                .await(SdxClusterStatusResponse.RUNNING, key(sdxInternal))
                .then((tc, testDto, client) -> {
                    return waitUtil.waitForSdxInstancesStatus(testDto, client, getSdxInstancesHealthyState());
                })
                .then((tc, dto, client) -> {
                    Log.log(LOGGER, format(" Image Catalog Name: %s ", dto.getResponse().getStackV4Response().getImage().getCatalogName()));
                    Log.log(LOGGER, format(" Image Catalog URL: %s ", dto.getResponse().getStackV4Response().getImage().getCatalogUrl()));
                    Log.log(LOGGER, format(" Image ID after SDX Upgrade: %s ", dto.getResponse().getStackV4Response().getImage().getId()));

                    if (dto.getResponse().getStackV4Response().getImage().getId().equals(customImageId)) {
                        throw new TestFailException(" SDX Image Update was not successful because of the actual and the previous Image IDs are same: "
                                + dto.getResponse().getStackV4Response().getImage().getId() + " = "
                                + customImageId);
                    }
                    return dto;
                })
                .validate();
    }

    protected Map<String, InstanceStatus> getSdxInstancesHealthyState() {
        return instancesHealthy;
    }

}
