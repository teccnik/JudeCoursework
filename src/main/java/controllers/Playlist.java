package controllers;

import server.Main;
import static server.Converter.convertToJSONArray;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;


import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Cookie;

import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLOutput;
import java.util.UUID;

@Path("playlist/")
@Consumes(MediaType.MULTIPART_FORM_DATA)
@Produces(MediaType.APPLICATION_JSON)


public class Playlist {
    @POST
    @Path("new")
    public String newPlaylist(@CookieParam("sessionToken") Cookie sessionToken, @FormDataParam("title") String title) {
        return title;
    }
}
