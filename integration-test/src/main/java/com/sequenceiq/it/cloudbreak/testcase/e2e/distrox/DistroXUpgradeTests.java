package com.sequenceiq.it.cloudbreak.testcase.e2e.distrox;

import static com.sequenceiq.it.cloudbreak.context.RunningParameter.key;

import javax.inject.Inject;

import com.sequenceiq.it.cloudbreak.exception.TestFailException;
import org.testng.annotations.Test;

import com.sequenceiq.it.cloudbreak.assertion.distrox.AwsAvailabilityZoneAssertion;
import com.sequenceiq.it.cloudbreak.client.DistroXTestClient;
import com.sequenceiq.it.cloudbreak.client.ImageCatalogTestClient;
import com.sequenceiq.it.cloudbreak.client.SdxTestClient;
import com.sequenceiq.it.cloudbreak.cloud.v4.CommonClusterManagerProperties;
import com.sequenceiq.it.cloudbreak.context.Description;
import com.sequenceiq.it.cloudbreak.context.TestContext;
import com.sequenceiq.it.cloudbreak.dto.distrox.DistroXTestDto;
import com.sequenceiq.it.cloudbreak.dto.distrox.cluster.DistroXUpgradeTestDto;
import com.sequenceiq.it.cloudbreak.dto.distrox.image.DistroXImageTestDto;
import com.sequenceiq.it.cloudbreak.dto.imagecatalog.ImageCatalogTestDto;
import com.sequenceiq.it.cloudbreak.dto.sdx.SdxTestDto;
import com.sequenceiq.it.cloudbreak.dto.sdx.SdxUpgradeTestDto;
import com.sequenceiq.it.cloudbreak.testcase.e2e.AbstractE2ETest;
import com.sequenceiq.it.cloudbreak.util.spot.UseSpotInstances;
import com.sequenceiq.sdx.api.model.SdxClusterStatusResponse;
import com.sequenceiq.sdx.api.model.SdxUpgradeReplaceVms;

public class DistroXUpgradeTests extends AbstractE2ETest {

    @Inject
    private SdxTestClient sdxTestClient;

    @Inject
    private DistroXTestClient distroXTestClient;

    @Inject
    private ImageCatalogTestClient imageCatalogTest;

    @Inject
    private CommonClusterManagerProperties commonClusterManagerProperties;

    private String uuid;

    @Override
    protected void setupTest(TestContext testContext) {
        createDefaultUser(testContext);
        createDefaultCredential(testContext);
        initializeDefaultBlueprints(testContext);
        createEnvironmentWithFreeIpa(testContext);
    }

    protected String getUuid(TestContext testContext, String prodCatalogName, String currentRuntimeVersion3rdParty) {
        testContext
                .given(ImageCatalogTestDto.class).withName(prodCatalogName)
                .when(imageCatalogTest.getV4(true));
        ImageCatalogTestDto dto = testContext.get(ImageCatalogTestDto.class);
        uuid = dto.getResponse().getImages().getCdhImages().stream()
                .filter(img -> img.getVersion().equals(currentRuntimeVersion3rdParty) && img.getImageSetsByProvider().keySet().iterator().next()
                        .equals(testContext.commonCloudProperties().getCloudProvider().toLowerCase())).iterator().next().getUuid();
        return uuid;
    }

    @Test(dataProvider = TEST_CONTEXT)
    @UseSpotInstances
    @Description(
            given = "there is a running Cloudbreak, and an environment with SDX and two DistroX clusters in " +
            "available state, one cluster created with deafult catalog and one cluster created with production catalog",
            when = "upgrade called on both DistroX clusters",
            then = "Both DistroX upgrade should be successful," + " the clusters should be up and running")
    public void testDistroXUpgrades(TestContext testContext) {
        String imageSettings = resourcePropertyProvider().getName();
        String currentRuntimeVersion = commonClusterManagerProperties.getUpgrade().getDistroXUpgradeCurrentVersion();
        String targetRuntimeVersion = commonClusterManagerProperties.getUpgrade().getDistroXUpgradeTargetVersion();
        String currentRuntimeVersion3rdParty = commonClusterManagerProperties.getUpgrade().getDistroXUpgrade3rdPartyCurrentVersion();
        String targetRuntimeVersion3rdParty = commonClusterManagerProperties.getUpgrade().getDistroXUpgrade3rdPartyTargetVersion();
        String sdxName = resourcePropertyProvider().getName();
        String distroXName = resourcePropertyProvider().getName();
        String distroXProdCatName = resourcePropertyProvider().getName();
        String prodCatalogName = resourcePropertyProvider().getName();
        testContext
                .given(sdxName, SdxTestDto.class)
                .withRuntimeVersion(currentRuntimeVersion)
                .withCloudStorage(getCloudStorageRequest(testContext))
                .when(sdxTestClient.create(), key(sdxName))
                .await(SdxClusterStatusResponse.RUNNING, key(sdxName))
                .awaitForHealthyInstances()
                .validate();
        testContext
                .given(distroXName, DistroXTestDto.class)
                .withTemplate(String.format(commonClusterManagerProperties.getInternalDistroXBlueprintType(), currentRuntimeVersion))
                .withPreferredSubnetsForInstanceNetworkIfMultiAzEnabledOrJustFirst()
                .when(distroXTestClient.create(), key(distroXName))
                .validate();
        createImageValidationSourceCatalog(testContext, commonClusterManagerProperties.getUpgrade()
                .getImageCatalogUrl3rdParty(), prodCatalogName);
        testContext
                .given(imageSettings, DistroXImageTestDto.class).withImageCatalog(prodCatalogName)
                .withImageId(getUuid(testContext, prodCatalogName, currentRuntimeVersion3rdParty))
                .given(distroXProdCatName, DistroXTestDto.class)
                .withTemplate(String.format(commonClusterManagerProperties.getInternalDistroXBlueprintType(), currentRuntimeVersion3rdParty))
                .withPreferredSubnetsForInstanceNetworkIfMultiAzEnabledOrJustFirst()
                .withImageSettings(imageSettings)
                .when(distroXTestClient.create(), key(distroXProdCatName))
                .await(STACK_AVAILABLE, key(distroXProdCatName))
                .awaitForHealthyInstances()
                .then((tc, testDto, client) -> checkImageId(testDto, uuid))
                .given(distroXName, DistroXTestDto.class)
                .await(STACK_AVAILABLE, key(distroXName))
                .awaitForHealthyInstances()
                .then(new AwsAvailabilityZoneAssertion())
                .validate();
        testContext
                .given(distroXName, DistroXTestDto.class)
                .when(distroXTestClient.stop(), key(distroXName))
                .given(distroXProdCatName, DistroXTestDto.class)
                .when(distroXTestClient.stop(), key(distroXProdCatName))
                .await(STACK_STOPPED, key(distroXProdCatName))
                .given(distroXName, DistroXTestDto.class)
                .await(STACK_STOPPED, key(distroXName))
                .validate();
        testContext
                .given(SdxUpgradeTestDto.class)
                .withReplaceVms(SdxUpgradeReplaceVms.DISABLED)
                .withRuntime(targetRuntimeVersion)
                .given(sdxName, SdxTestDto.class)
                .when(sdxTestClient.upgrade(), key(sdxName))
                .await(SdxClusterStatusResponse.DATALAKE_UPGRADE_IN_PROGRESS, key(sdxName).withWaitForFlow(Boolean.FALSE))
                .await(SdxClusterStatusResponse.RUNNING, key(sdxName))
                .awaitForHealthyInstances()
                .validate();
        testContext
                .given(distroXName, DistroXTestDto.class)
                .when(distroXTestClient.start(), key(distroXName))
                .given(distroXProdCatName, DistroXTestDto.class)
                .when(distroXTestClient.start(), key(distroXProdCatName))
                .await(STACK_AVAILABLE, key(distroXProdCatName))
                .given(distroXName, DistroXTestDto.class)
                .await(STACK_AVAILABLE, key(distroXName))
                .awaitForHealthyInstances()
                .validate();
        testContext
                .given(DistroXUpgradeTestDto.class)
                .withRuntime(targetRuntimeVersion)
                .given(distroXName, DistroXTestDto.class)
                .when(distroXTestClient.upgrade(), key(distroXName))
                .given(DistroXUpgradeTestDto.class)
                .withRuntime(targetRuntimeVersion3rdParty)
                .given(distroXProdCatName, DistroXTestDto.class)
                .when(distroXTestClient.upgrade(), key(distroXProdCatName))
                .await(STACK_AVAILABLE, key(distroXProdCatName))
                .awaitForHealthyInstances()
                .given(distroXName, DistroXTestDto.class)
                .await(STACK_AVAILABLE, key(distroXName))
                .awaitForHealthyInstances()
                .then(new AwsAvailabilityZoneAssertion())
                .validate();
    }

    private DistroXTestDto checkImageId(DistroXTestDto testDto, String imageId) {
        if (!testDto.getResponse().getImage().getId().equals(imageId)) {
            throw new TestFailException(" The selected image ID is: " + testDto.getResponse().getImage().getId() + " instead of: "
                    + imageId);
        }
        return testDto;
    }
}