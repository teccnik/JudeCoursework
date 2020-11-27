package controllers;

import server.Main;
import static server.Converter.convertToJSONObject;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.json.simple.JSONObject;



import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Cookie;

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
    public String uploadSong(@CookieParam("sessionToken") Cookie sessionToken,@FormDataParam("file") InputStream uploadedInputStream, @FormDataParam("file") FormDataContentDisposition fileDetail) throws Exception {
        System.out.println("Invoked song.uploadSong()");

        String fileName = fileDetail.getFileName();
        int dot = fileName.lastIndexOf('.');
        String fileExtension = fileName.substring(dot+1);
        String newFileName = "client/music/" + UUID.randomUUID() + "." + fileExtension;

        int userID = validateSessionToken(sessionToken);
        if (userID==-1) {
            return "Error: Could not validate user";
        }

        PreparedStatement statement = Main.db.prepareStatement("UPDATE Songs SET songFile = ? WHERE userID = ?");
        statement.setString(1,newFileName);
        statement.setInt(2,userID);
        statement.executeUpdate();

        String uploadedFileLocation = "";

        try {
            int read = 0;
            byte[] bytes = new byte[1024];
            OutputStream out = new FileOutputStream(new File(uploadedFileLocation));
            while ((read = uploadedInputStream.read(bytes)) != -1) {
                out.write(bytes,0,read);
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
    public static boolean validateSessionToken(String sessionToken) {
        try {
            PreparedStatement ps = Main.db.prepareStatement("SELECT userID FROM Users WHERE sessionToken =?");
            ps.setString(1,sessionToken);
            ResultSet logoutResults = ps.executeQuery();
            return logoutResults.next();
        } catch (Exception exception) {
            System.out.println("Database error" + exception.getMessage());
            return false;
        }
    }
}
