package controllers;

import server.Main;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;


import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Cookie;

import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

@Path("song/")
@Consumes(MediaType.MULTIPART_FORM_DATA)
@Produces(MediaType.APPLICATION_JSON)

public class Song {
    @POST
    @Path("new")
    public String uploadSong(@CookieParam("sessionToken") Cookie sessionToken, @FormDataParam("file") InputStream uploadedInputStream,
                             @FormDataParam("file") FormDataContentDisposition fileDetail) throws Exception {

        System.out.println("Invoked song.uploadSong()");

        String fileName = fileDetail.getFileName();
        int dot = fileName.lastIndexOf('.');
        String fileExtension = fileName.substring(dot + 1);
        String newFileName = "client/audio/" + UUID.randomUUID() + "." + fileExtension;

        int userID = validateSessionCookie(sessionToken);
        if (userID == -1) {
            return "{\"Error\": \"Could not validate user. \"}";
        }

        try {

            //THIS BIT FAILS BECAUSE OF NOT NULL CONYTRAINTS ON THE FIELD AND I"VE JUST TESTED UPLOADING THE FILE
            PreparedStatement statement = Main.db.prepareStatement("UPDATE Songs SET songFile = ? WHERE userID = ?");
            statement.setString(1, newFileName);
            statement.setInt(2, userID);
            statement.executeUpdate();
        } catch (Exception exception) {
            System.out.println("Database error: " + exception.getMessage());
            return "{\"Error\": \"Unable to list items.  Error code xx.\"}";

        }


        String uploadedFileLocation = "C:\\Users\\Rachel\\IdeaProjects\\JudeLCourseworkv2\\resources\\" + newFileName;   // change as appropriate

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

}
