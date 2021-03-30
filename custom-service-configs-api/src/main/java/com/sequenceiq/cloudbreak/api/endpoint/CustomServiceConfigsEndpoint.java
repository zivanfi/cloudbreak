package com.sequenceiq.cloudbreak.api.endpoint;


import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sequenceiq.cloudbreak.doc.OperationDescriptions.CustomServiceConfigsOpDescription;
import com.sequenceiq.cloudbreak.domain.CustomServiceConfigs;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Path("/v4/customserviceconfigs")
@Consumes(MediaType.APPLICATION_JSON)
@Api(value = "/v4/customserviceconfigs", protocols = "http,https", consumes = MediaType.APPLICATION_JSON)
public interface CustomServiceConfigsEndpoint {
    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = CustomServiceConfigsOpDescription.GET_ALL, produces = MediaType.APPLICATION_JSON, nickname = "listCustomServiceConfigs")
    List<CustomServiceConfigs> listCustomServiceConfigs();

    @GET
    @Path("/crn/{crn}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = CustomServiceConfigsOpDescription.GET_BY_CRN, produces = MediaType.APPLICATION_JSON, nickname = "listCustomConfigsByCrn")
    CustomServiceConfigs listCustomServiceConfigsByCrn(@PathParam("crn") String crn);

    @GET
    @Path("/name/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = CustomServiceConfigsOpDescription.GET_BY_NAME, produces = MediaType.APPLICATION_JSON, nickname = "listCustomServiceConfigsByName")
    CustomServiceConfigs listCustomServiceConfigsByName(@PathParam("name") String name);

    @POST
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = CustomServiceConfigsOpDescription.CREATE, produces = MediaType.APPLICATION_JSON, nickname = "addCustomServiceConfigs")
    String addCustomServiceConfigs(CustomServiceConfigs customServiceConfigs);

    @PUT
    @Path("/crn/{crn}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = CustomServiceConfigsOpDescription.UPDATE_BY_CRN, produces = MediaType.APPLICATION_JSON, nickname = "updateCustomServiceConfigsByCrn")
    String updateCustomServiceConfigsByCrn(@PathParam("crn") String crn, String customServiceConfigsText);

    @PUT
    @Path("/name/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = CustomServiceConfigsOpDescription.UPDATE_BY_NAME, produces = MediaType.APPLICATION_JSON, nickname = "updateCustomServiceConfigsByName")
    String updateCustomServiceConfigsByName(@PathParam("name") String name, String customServiceConfigsText);

    @DELETE
    @Path("/crn/{crn}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = CustomServiceConfigsOpDescription.DELETE_BY_CRN, produces = MediaType.APPLICATION_JSON, nickname = "deleteCustomServiceConfigsByCrn")
    CustomServiceConfigs deleteCustomServiceConfigsByCrn(@PathParam("crn") String crn);

    @DELETE
    @Path("/name/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = CustomServiceConfigsOpDescription.DELETE_BY_NAME, produces = MediaType.APPLICATION_JSON, nickname = "deleteCCustomServiceConfigsByName"
    )
    CustomServiceConfigs deleteCustomServiceConfigsByName(@PathParam("name") String name);
}
