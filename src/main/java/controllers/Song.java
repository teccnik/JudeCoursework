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



@Path("song/")
@Consumes(MediaType.MULTIPART_FORM_DATA)
@Produces(MediaType.APPLICATION_JSON)

public class Song {
    @POST
    @Path("new")
    public String uploadSong(@CookieParam("sessionToken") Cookie sessionToken, @FormDataParam("file") InputStream uploadedInputStream,
                             @FormDataParam("file") FormDataContentDisposition fileDetail, @FormDataParam("songName") String songName) throws Exception {

        System.out.println("Invoked song.uploadSong()");

        String fileName = fileDetail.getFileName();
        int dot = fileName.lastIndexOf('.');
        String fileExtension = fileName.substring(dot + 1); //Comments
        UUID fileUUID = UUID.randomUUID();
        String newFileName = "client/audio/" + fileUUID + "." + fileExtension;
        String savedFileName = "audio/"+fileUUID+"."+fileExtension;

        int userID = validateSessionCookie(sessionToken);
        if (userID == -1) {
            return "{\"Error\": \"Could not validate user. \"}";
        }

        try {
            PreparedStatement statement = Main.db.prepareStatement("INSERT INTO Songs (userID,songName,songFile) VALUES(?,?,?)");
            PreparedStatement statement2 = Main.db.prepareStatement("INSERT INTO Analytics(likes,comments,saves) VALUES(0,0,0)");
            statement.setInt(1, userID);
            statement.setString(2, fileName.substring(0, dot));  //set song name to name of file less the extension
            statement.setString(3, savedFileName);
            statement.executeUpdate();
            statement2.executeUpdate();
        } catch (Exception e) {
            System.out.println("Database error: " + e.getMessage());
            return "{\"Error\": \"Unable to list items.  Error code xx.\"}";

        }
        String uploadedFileLocation = "C:\\Users\\Rachel\\IdeaProjects\\JudeLCourseworkv2\\resources\\" + newFileName;   // change as appropriate change as appropriate
        System.out.println(uploadedFileLocation);
        try {
            int read = 0;
            byte[] bytes = new byte[1024];
            OutputStream out = new FileOutputStream(new File(uploadedFileLocation));
            while ((read = uploadedInputStream.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            out.flush();
            out.close();
            System.out.println("File uploaded to server & path saved to database.");
            return "File uploaded to server and path saved to database";
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            return "{\"Error\": \"Something went wrong. \"}";
        }
    }


    public static int validateSessionCookie(Cookie sessionCookie) {     //returns the userID that of the record with the cookie value

        String token = sessionCookie.getValue();
        System.out.println("Invoked User.validateSessionCookie(), cookie value " + token);

        try {
            PreparedStatement statement = Main.db.prepareStatement(
                    "SELECT userID FROM Users WHERE SessionToken = ?"
            );
            statement.setString(1, token);
            ResultSet resultSet = statement.executeQuery();
            System.out.println("userID is " + resultSet.getInt("UserID"));
            return resultSet.getInt("UserID");  //Retrieve by column name  (should really test we only get one result back!)
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return -1;  //rogue value indicating error

        }
    }


    @GET
    @Path("getSong")
    public String getSong() {
        System.out.println("Song.getSong() Invoked.");
        try {
            PreparedStatement statement = Main.db.prepareStatement("SELECT songID, userID, songName, songLength, songFile FROM Songs");
            ResultSet resultSet = statement.executeQuery();
            JSONArray response = convertToJSONArray(resultSet);
            return response.toString();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "{\"Error\": \"Something went wrong. Please contact admin with code S-GS. \"}";
        }
    }
    @GET
    @Path("getSong/{searchString}")
    public String getSong(@PathParam("searchString") String searchString) {
        System.out.println("Song.getSong() Invoked with searchString = " +searchString);
        try {
            PreparedStatement statement = Main.db.prepareStatement("SELECT songID, userID, songName, songLength, songFile FROM Songs WHERE songName LIKE ?");
            PreparedStatement statement2 = Main.db.prepareStatement("SELECT Users.username FROM Users WHERE userID = (SELECT Songs.userID FROM Songs WHERE songName LIKE ?)");

            statement.setString(1,'%' + searchString.toLowerCase()+'%'); //% is wildcard for finding strings of shared characters
            ResultSet resultSet = statement.executeQuery();
            JSONArray response = convertToJSONArray(resultSet);
            System.out.println(response);

            statement2.setString(1,'%' + searchString.toLowerCase()+'%');
            ResultSet resultSet2 = statement2.executeQuery();
            JSONArray response2 = convertToJSONArray(resultSet2);
            System.out.println(response2);

            //statement2.setString(2,resultSet.userID);

            JSONObject songDetails = new JSONObject();
            songDetails.put("details",response);
            songDetails.put("username",response2);

            System.out.println(response);
            return response.toString();
            //return songDetails.toString();

            //statement2.setString(2, response.);

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "{\"Error\": \"Something went wrong. Please contact admins with code S-GS. \"}";
        }
    }
    @GET
    @Path("getTrending")
    public String getTrending() {
        System.out.println("Song.getTrending() Invoked.");
        try {
            PreparedStatement statement = Main.db.prepareStatement("SELECT TOP 10 * FROM Analytics ORDER BY likes DESC");
            ResultSet resultSet = statement.executeQuery();
            JSONArray response = convertToJSONArray(resultSet);
            System.out.println(response);

            JSONObject trendingList = new JSONObject();
            trendingList.put("songs",response);

            return response.toString();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "{\"Error\": \"Something went wrong. Please contact admins with code S-GT. \"}";
        }
    }
    @POST
    @Path("trackPlay")
    public String trackPlay(@FormDataParam("songID") Integer songID) {
        System.out.println("Songs.trackPlay() Invoked.");
        try {
            PreparedStatement statement = Main.db.prepareStatement("UPDATE Analytics SET plays = plays + 1 WHERE songID = ?");
            statement.setInt(1, songID);
            statement.executeUpdate();
            return "{\"SUCCESS\": \"Play successfully added. \"}";
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "{\"Error\": \"Something went wrong. Please contact admins with code S-TP. \"}";
        }
    }

}
