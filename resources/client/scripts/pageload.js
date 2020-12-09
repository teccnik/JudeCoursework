function pageLoad() {
    console.log("Invoked pageLoad()");
    const url="user/validateSessionToken";

    fetch(url, {
        method: "POST",
    }).then(response => {
        return response.json();
    }).then(response => {
        if(response.hasOwnProperty("Error")) {
            console.log("Not Logged In.");
            document.getElementById("user-nav").innerHTML="";
        } else {
            console.log("Logged in.");
            document.getElementById("login-nav").innerHTML="";
        }
    });
}