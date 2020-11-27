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


@Path("user/")
@Consumes(MediaType.MULTIPART_FORM_DATA)
@Produces(MediaType.APPLICATION_JSON)

public class User {
    @POST
    @Path("login")
    public String userLogin(@FormDataParam("username") String username, @FormDataParam("password") String password) {
        System.out.println("User.login() invoked with username " + username + " and password " + password);

        try {
            PreparedStatement ps1 = Main.db.prepareStatement("SELECT password FROM Users WHERE username = ?");
            ps1.setString(1,username);
            ResultSet loginResults = ps1.executeQuery();
            if (loginResults.next() == true) {
                String correctPassword = loginResults.getString(1);
                if (password.equals(correctPassword)) {
                    String sessionToken = UUID.randomUUID().toString();
                    PreparedStatement ps2 = Main.db.prepareStatement("UPDATE Users SET sessionToken = ? WHERE username = ?");
                    ps2.setString(1,sessionToken);
                    ps2.setString(2, username);
                    ps2.executeUpdate();
                    JSONObject userDetails = new JSONObject();
                    userDetails.put("username",username);
                    userDetails.put("sessionToken",sessionToken);
                    return userDetails.toString();
                } else {
                    return "{\"Error\": \"Incorrect Password!\"}";
                }
            } else {
                return "{\"Error\": \"Username & Password are incorrect. \"}";
            }
        } catch (Exception exception) {
            System.out.println("Database error during /user/login: " + exception.getMessage());
            return "{\"Error\": \"Server side error.\"}";
        }

    }
    @POST
    @Path("userid")
    public static int getUserID(@FormDataParam("username") String username, @FormDataParam("password") String password) {
        System.out.println("User.getUserID() Invoked");
        try {
            PreparedStatement statement = Main.db.prepareStatement( "SELECT UserID FROM Users WHERE username = ? AND password = ?");
            statement.setString(1, username);
            statement.setString(2, password);
            ResultSet resultSet = statement.executeQuery();

            if (!resultSet.next()) {
                return -1;
            } else {
                return resultSet.getInt("UserID");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return -1;
        }
    }

    public static String updateUUIDinDB(int userID, String UUID) {
        System.out.println("User.updateUUIDinDB() Invoked");
        try {
            PreparedStatement statement = Main.db.prepareStatement("UPDATE Users SET sessionToken = ? WHERE UserID = ?");
            statement.setString(1,UUID);
            statement.setInt(2,userID);
            statement.executeUpdate();
            return "OK";
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "Error";
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

    @POST
    @Path("new")
    public String userNew(@FormDataParam("username") String username, @FormDataParam("email") String email, @FormDataParam("password") String password) {
        System.out.println("User.userNew() Invoked");

        try {
            PreparedStatement statement = Main.db.prepareStatement(
                    "INSERT INTO Users (username, email, password) VALUES (?,?,?)");

            statement.setString(1, username);
            statement.setString(2, email);
            statement.setString(3, password);
            statement.executeUpdate();
            return "{\"SUCCESS\": \"New User added successfully. \"}";

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "{\"Error\": \"Something went wrong. Please contact an admin. \"}";

        }
    }
    @GET
    @Path("name")
    public String userName(@CookieParam("sessionToken") Cookie sessionToken) {
        System.out.println("User.userName() Invoked");
        if (sessionToken==null) {
            return "{\"Error\": \"Something went wrong. Please contact an admin. \"}";
        }
        try {
            String uuid = sessionToken.getValue();
            PreparedStatement statement = Main.db.prepareStatement("SELECT username FROM Users WHERE sessionToken = ?");
            statement.setString(1,uuid);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.getString("username");
        } catch (Exception e) {
            return "{\"Error\": \"Something went wrong. Please contact an admin.\"}";
        }
    }
    @GET
    @Path("get")
    public String userGet(@CookieParam("sessionToken") Cookie sessionToken) {
        System.out.println("User.userGet() has been Invoked.");
        if (sessionToken==null) {
            return "{\"Error\": \"Something went wrong. Please contact an admin. (U-UGT)\"}";
        }
        try {
            String uuid = sessionToken.getValue();
            PreparedStatement statement = Main.db.prepareStatement("SELECT * FROM Users WHERE sessionToken = ?");
            statement.setString(1, uuid);
            ResultSet resultSet = statement.executeQuery();
            JSONObject resultsJSON = convertToJSONObject(resultSet);
            return resultsJSON.toString();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "{\"Error\": \"Something went wrong! Please contact an admin. (U-UGT)\"}";
        }
    }
    /*@POST
    @Path("update")
    public String userUpdate(@CookieParam("sessionToken") Cookie sessionToken, @FormDataParam("username") String username, @FormDataParam("email") String email, @FormDataParam("password") String password) {
        System.out.println("User.userUpdate() has been Invoked.");
        if (sessionToken==null) {
            return "{\"Error\": \"Something went wrong. Contact an admin. (U-UPD)\"}";
        }
        try {
            int userID = User.validateSessionToken(sessionToken);
            PreparedStatement statement = Main.db.prepareStatement("UPDATE Users SET username = ?, email = ?, password = ?");
            statement.setString(1, username);
            statement.setString(2, email);
            statement.setString(3, password);
            statement.executeUpdate();
            return "Success";
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "{\"Error\": \"Something went wrong. Contact an admin. (U-UPD)\"}";
        }
    }
    */

    @GET
    @Path("delete")
    public String userDelete(@CookieParam("sessionToken") Cookie sessionToken) {
        System.out.println("User.userDelete() Invoked.");
        try {
            PreparedStatement statement = Main.db.prepareStatement("DELETE FROM Users WHERE sessionToken = ?");
            statement.setString(1,sessionToken.getValue());
            statement.executeUpdate();
            return "{\"Success\": \"User Deleted.\"}";
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "{\"Error\": \"Something went wrong. Contact an admin. (U-UDT).\"}";
        }
    }
}
