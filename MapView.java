package com.boattracking;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import netscape.javascript.JSObject;

public class MapView extends Application {

    private static BoatDetectionSystem system;
    private static final int WIDTH = 1200;
    private static final int HEIGHT = 800;

    private static final double MIN_LAT = BoatDetectionSystem.MIN_LAT;
    private static final double MAX_LAT = BoatDetectionSystem.MAX_LAT;
    private static final double MIN_LON = BoatDetectionSystem.MIN_LON;
    private static final double MAX_LON = BoatDetectionSystem.MAX_LON;
    
    private WebEngine webEngine;

    public static void setSystem(BoatDetectionSystem sys) {
        system = sys;
    }

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        
        // Create WebView for interactive map
        WebView webView = new WebView();
        webEngine = webView.getEngine();
        
        // Load the HTML map
        webEngine.loadContent(generateMapHTML());
        
        // Set up auto-refresh to update boat positions every 2 seconds
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(2), event -> {
            updateBoatMarkers();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        
        // Wait for page to load before starting updates
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                updateBoatMarkers();
                timeline.play();
            }
        });
        
        root.setCenter(webView);
        
        stage.setTitle("Interactive Boat Tracking Map - Red Sea Region");
        stage.setScene(new Scene(root, WIDTH, HEIGHT));
        stage.show();
    }
    
    private void updateBoatMarkers() {
        if (system == null || webEngine == null) return;
        
        StringBuilder js = new StringBuilder("updateBoats([");
        boolean first = true;
        
        for (Boat boat : system.getAllBoats()) {
            if (!first) js.append(",");
            first = false;
            
            String color = switch (boat.getStatus()) {
                case GREEN -> "green";
                case YELLOW -> "orange";
                case RED -> "red";
            };
            
            js.append(String.format(
                "{id:'%s',lat:%.6f,lon:%.6f,status:'%s',color:'%s'}",
                boat.getId(),
                boat.getLatitude(),
                boat.getLongitude(),
                boat.getStatus(),
                color
            ));
        }
        
        js.append("]);");
        webEngine.executeScript(js.toString());
    }

    private String generateMapHTML() {
        double centerLat = (MIN_LAT + MAX_LAT) / 2;
        double centerLon = (MIN_LON + MAX_LON) / 2;
        
        return "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "    <meta charset='utf-8'>\n" +
            "    <title>Boat Tracking Map</title>\n" +
            "    <link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'/>\n" +
            "    <script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>\n" +
            "    <style>\n" +
            "        body { margin: 0; padding: 0; }\n" +
            "        #map { width: 100vw; height: 100vh; }\n" +
            "        .boat-marker {\n" +
            "            width: 30px;\n" +
            "            height: 30px;\n" +
            "            border-radius: 50%;\n" +
            "            border: 3px solid white;\n" +
            "            box-shadow: 0 0 10px rgba(0,0,0,0.5);\n" +
            "            display: flex;\n" +
            "            align-items: center;\n" +
            "            justify-content: center;\n" +
            "            font-weight: bold;\n" +
            "            color: white;\n" +
            "            font-size: 16px;\n" +
            "        }\n" +
            "        .legend {\n" +
            "            position: absolute;\n" +
            "            bottom: 30px;\n" +
            "            right: 10px;\n" +
            "            background: white;\n" +
            "            padding: 10px;\n" +
            "            border-radius: 5px;\n" +
            "            box-shadow: 0 0 15px rgba(0,0,0,0.2);\n" +
            "            z-index: 1000;\n" +
            "            font-family: Arial;\n" +
            "        }\n" +
            "        .legend-item {\n" +
            "            display: flex;\n" +
            "            align-items: center;\n" +
            "            margin: 5px 0;\n" +
            "        }\n" +
            "        .legend-color {\n" +
            "            width: 20px;\n" +
            "            height: 20px;\n" +
            "            border-radius: 50%;\n" +
            "            margin-right: 10px;\n" +
            "            border: 2px solid white;\n" +
            "            box-shadow: 0 0 5px rgba(0,0,0,0.3);\n" +
            "        }\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <div id='map'></div>\n" +
            "    <div class='legend'>\n" +
            "        <h4 style='margin: 0 0 10px 0;'>Boat Status</h4>\n" +
            "        <div class='legend-item'>\n" +
            "            <div class='legend-color' style='background-color: green;'></div>\n" +
            "            <span>Normal (GREEN)</span>\n" +
            "        </div>\n" +
            "        <div class='legend-item'>\n" +
            "            <div class='legend-color' style='background-color: orange;'></div>\n" +
            "            <span>Warning (YELLOW)</span>\n" +
            "        </div>\n" +
            "        <div class='legend-item'>\n" +
            "            <div class='legend-color' style='background-color: red;'></div>\n" +
            "            <span>Alert (RED)</span>\n" +
            "        </div>\n" +
            "    </div>\n" +
            "    <script>\n" +
            "        var map = L.map('map').setView([" + centerLat + ", " + centerLon + "], 8);\n" +
            "        \n" +
            "        // Add OpenStreetMap tiles\n" +
            "        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {\n" +
            "            attribution: '© OpenStreetMap contributors',\n" +
            "            maxZoom: 19\n" +
            "        }).addTo(map);\n" +
            "        \n" +
            "        // Draw permitted area boundary\n" +
            "        var bounds = [[" + MIN_LAT + ", " + MIN_LON + "], [" + MAX_LAT + ", " + MAX_LON + "]];\n" +
            "        L.rectangle(bounds, {\n" +
            "            color: 'blue',\n" +
            "            weight: 3,\n" +
            "            fillOpacity: 0.1,\n" +
            "            dashArray: '10, 10'\n" +
            "        }).addTo(map).bindPopup('Permitted Operating Area');\n" +
            "        \n" +
            "        // Draw restricted zone\n" +
            "        var restrictedBounds = [[20.5, 40.5], [21.0, 41.0]];\n" +
            "        L.rectangle(restrictedBounds, {\n" +
            "            color: 'red',\n" +
            "            weight: 2,\n" +
            "            fillColor: 'red',\n" +
            "            fillOpacity: 0.2\n" +
            "        }).addTo(map).bindPopup('Restricted Zone - No Entry');\n" +
            "        \n" +
            "        var markers = {};\n" +
            "        \n" +
            "        function updateBoats(boats) {\n" +
            "            // Remove old markers\n" +
            "            for (var id in markers) {\n" +
            "                if (!boats.find(b => b.id === id)) {\n" +
            "                    map.removeLayer(markers[id]);\n" +
            "                    delete markers[id];\n" +
            "                }\n" +
            "            }\n" +
            "            \n" +
            "            // Add or update markers\n" +
            "            boats.forEach(function(boat) {\n" +
            "                if (markers[boat.id]) {\n" +
            "                    // Update existing marker\n" +
            "                    markers[boat.id].setLatLng([boat.lat, boat.lon]);\n" +
            "                    markers[boat.id].setPopupContent(\n" +
            "                        '<b>Boat ID:</b> ' + boat.id + '<br>' +\n" +
            "                        '<b>Status:</b> ' + boat.status + '<br>' +\n" +
            "                        '<b>Position:</b> ' + boat.lat.toFixed(4) + ', ' + boat.lon.toFixed(4)\n" +
            "                    );\n" +
            "                } else {\n" +
            "                    // Create new marker\n" +
            "                    var icon = L.divIcon({\n" +
            "                        className: 'boat-marker',\n" +
            "                        html: '<div class=\"boat-marker\" style=\"background-color:' + boat.color + ';\">&#9973;</div>',\n" +
            "                        iconSize: [30, 30]\n" +
            "                    });\n" +
            "                    \n" +
            "                    var marker = L.marker([boat.lat, boat.lon], {icon: icon})\n" +
            "                        .addTo(map)\n" +
            "                        .bindPopup(\n" +
            "                            '<b>Boat ID:</b> ' + boat.id + '<br>' +\n" +
            "                            '<b>Status:</b> ' + boat.status + '<br>' +\n" +
            "                            '<b>Position:</b> ' + boat.lat.toFixed(4) + ', ' + boat.lon.toFixed(4)\n" +
            "                        );\n" +
            "                    \n" +
            "                    markers[boat.id] = marker;\n" +
            "                }\n" +
            "            });\n" +
            "        }\n" +
            "    </script>\n" +
            "</body>\n" +
            "</html>";
    }
}
