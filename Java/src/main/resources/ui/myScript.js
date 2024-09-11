function changeColor(e) {
   console.log(e);
   console.log(e.style.fill);
   var currentColor = e.style.fill;

   if (currentColor === "red") {
      e.style.fill = "green"
   } else {
      e.style.fill = "red"
   }
}

function updateParkplatzStatus() {
    console.log("update parkplatz status")
    const request = new XMLHttpRequest();
    request.open("GET", "/state/all", false); // `false` makes the request synchronous
    request.send(null);

    if (request.status === 200) {
        console.log(request.responseText);
        displayParkplatzStatus(JSON.parse(request.responseText));
    }
};


function displayParkplatzStatus(status) {
    status.forEach((element) => applyColor(element.number, element.status));
}

function applyColor(number ,status) {
    if (status === 0) {
        elementID = "Availability-PL-"+number;
        doof = document.getElementById(elementID);
        // Setze Ampelfarbe auf grün
        document.getElementById("Availability-PL-"+number).style.fill = "green";
    } else {
       // Setze Ampelfarbe auf rot
       document.getElementById("Availability-PL-"+ number).style.fill = "red";
    }
};


