package controllers;

import server.Main;
import static server.Converter.convertToJSONArray;
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
    public String newPlaylist(@CookieParam("sessionToken") String sessionToken, @FormDataParam("title") String title) {
        int userID = validateSessionToken(sessionToken);
        try {
            PreparedStatement ps1 = Main.db.prepareStatement("INSERT INTO Playlists (userID, playlistName) VALUES (?,?)");
            ps1.setInt(1,userID);
            ps1.setString(2,title);
            ps1.executeUpdate();
            return "{\"SUCCESS\": \"New Playlist made. \"}";
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "{\"Error\": \"Something went wrong. Please contact an admin with code P-NE. \"}";
        }
    }
    @POST
    @Path("validateSessionToken")
    public static int validateSessionToken(@CookieParam("sessionToken") String sessionToken) {
        System.out.println("Invoked playlist.validateSessionCookie(), cookie value " + sessionToken);

        try {
            PreparedStatement statement = Main.db.prepareStatement(
                    "SELECT userID FROM Users WHERE sessionToken =?"
            );
            statement.setString(1,sessionToken);
            ResultSet resultSet = statement.executeQuery();
            System.out.println("userID is "+resultSet.getInt("UserID"));
            return resultSet.getInt("UserID");
        } catch (Exception e) {
            System.out.println("Database error" + e.getMessage());
            return -1; // rogue value for errors
        }
    }
    @GET
    @Path("get")
    public String getPlaylist(@CookieParam("sessionToken") String sessionToken) {
        System.out.println("Playlist.getPlaylist() was Invoked.");
        int userID = validateSessionToken(sessionToken);
        System.out.println(userID);
        if (userID==-1) {
            return "{\"Error\": \"Could not validate user. \"}";
        }
        try {
            PreparedStatement ps1 = Main.db.prepareStatement("SELECT playlistID,playlistName FROM Playlists WHERE userID = ?");
            ps1.setInt(1,userID);
            ResultSet resultSet = ps1.executeQuery();
            JSONArray response = convertToJSONArray(resultSet);
            System.out.println(response);
            return response.toString();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "{\"Error\": \"Something went wrong. Please contact admin with code P-GP. \"}";
        }
    }
    @GET
    @Path("getSongs/{playlistID}")
    public String getSongs(@PathParam("playlistID") int playlistID, @CookieParam("sessionToken") String sessionToken) {
        System.out.println("Playlist.getSongs() was Invoked for playlistID " + playlistID);
        int userID = validateSessionToken(sessionToken);
        if (userID==-1) {
            return "{\"Error\": \"Could not validate user. \"}";
        }
        try {
            PreparedStatement ps1 = Main.db.prepareStatement("SELECT Songs.userID, Songs.songName, Songs.songFile FROM Songs JOIN PlaylistSongs on Songs.SongID = PlaylistSongs.songID  JOIN Users ON Users.userID = Songs.userID  WHERE PlaylistSongs.playlistID = ?");
            ps1.setInt(1,playlistID);
            ResultSet resultSet = ps1.executeQuery();
            JSONArray response = convertToJSONArray(resultSet);
            System.out.println(response.toString());


            /*
            PreparedStatement ps2 = Main.db.prepareStatement("SELECT Users.username FROM Users WHERE userID = (SELECT Songs.userID From Songs WHERE songID = (SELECT PlaylistSongs.songID FROM PlaylistSongs = ?))");
            ps2.setInt(1,playlistID);
            ResultSet resultSet2 = ps2.executeQuery();
            JSONArray r2 = convertToJSONArray(resultSet2);
            System.out.println(r2.toString());
            */



            //System.out.println(r1.addAll(r2));
            return response.toString();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "{\"Error\": \"Something went wrong. Please contact admin with code P-GS. \"}";
        }
    }
}
