// This function displays a bunch of stations by making successive calls to
// our service.
function loadStationsOneAtATime(a) {
    var base = '/weather/api/stationHTML/';

    // INSERT THE STATION ID'S AND DESCRIPTIONS HERE
    var obj = {
        "MD5719": "Junction City, Near Meadowview",
        "KORAUMSV5": "Aumsville (Doornenbal)"
    };

    // Loop the obj for each station to display
    $.each( obj, function( key, value ) {
            //ret = "<div class='contents' id='" + id + "'>";

        $.ajax({
            url: base + key + "/" + value,
            type: 'GET',
        }).done(function ( data ) {
            //$("#" + a).append("<div class='contents' id='" + key + "'><b>" + value + " (" + key + ")</b><br>");
            $("#" + a).append(data);
            //$("#" + a).append("</div>");
        });
    });
}
