# Graphium Stop Detection

This Graphium application takes a GPX trajectory and detects stops. The road graph is used to derive some of the attributes (e.g. distance of stop to next road segment)

Details of the stop detection algorithm will be published soon.

## Quickstart

1. Follow the deployment and configuration of Graphium-Neo4j (https://github.com/graphium-project/graphium-neo4j#neo4j-server-plugins)
2. Build this project (Graphium Stop Detection)
3. Copy the jar-file of the plugin into the plugins-directory of your Neo4j server.
4. Download road dataset for your region and convert it to Graphium JSON format (see https://github.com/graphium-project/graphium#graph-data-conversion)
5. Import road dataset to Graphium Neo4j and activate it (see https://github.com/graphium-project/graphium-neo4j#configuration-of-neo4j)
6. Run stop detection with one (only high level road) or two graphs (high and low level graph)


    curl -X POST "http://localhost:7474/graphium/detectstops?graphHighLevel=osm_at_highlevel&graphLowLevel=osm_at_lowlevel" -F "file=@D:/path/to/gpx/file.gpx" -o d:/path/to/output.csv
    
    curl -X POST "http://localhost:7474/graphium/graphs/osm_at_highlevel/detectstops" -F "file=@D:/path/to/gpx/file.gpx" -o d:/path/to/output.csv

Note: The timestamps in the GPX-files may not contain time zones

8. A CSV-file is created containing all stops with all attributes.

## Stop attributes

The following stop attributes are derived for each stop. These attributes can be used to classify stops (e.g. to traffic-related and non-traffic-related stops).

 | Attribute                   | Description     |
 | --------------------------- | --------------- |
 | track_id                    | Id of the track |
 | placeId                     | Id of the place |
 | placeGeom                   | Point geometry of the place |
 | stayId                      | Id of the stop |
 | stayGeom                    | Point geometry of the stop |
 | startTime                   | Start time of the stop |
 | endTime                     | End time of the stop |
 | duration                    | Duration of the stop |
 | startIndex                  | First point index of the trajectory within the stop |
 | endIndex                    | Last point index of the trajectory within the stop |
 | stopDistance                | Distance during the stop |
 | avgSpeed                    | Average speed during the stop |
 | minimumSpeed                | Minimum speed during the stop |
 | avgSamplingInterval         | Average sampling interval |
 | summedAngles                | Sum of all direction changes based on the simplified stop geometry |
 | significantDirectionChanges | Number of direction changes based on the simplified stop geometry |
 | simpleGeom                  | Simplified stop geometry |
 | distanceSlow                | Stop distance where speed is less than 10 km/h |
 | numberOfStops               | Number of sections with less than 10 km/h within the stop |
 | distanceToNextHighLevelRoad | Distance to the next high level road segment |
 | frcOfNextHighLevelRoad      | Functional Road Class of the next high level road segment |
 | fowOfNextHighLevelRoad      | Form Of Way of the next high level road segment |
 | idOfNextHighLevelRoad       | ID of the next high level road segment |
 | distanceToNextLowLevelRoad  | Distance to the next low level road segment |
 | frcOfNextLowLevelRoad       | Functional Road Class of the next low level road segment |
 | fowOfNextLowLevelRoad       | Form Of Way of the next low level road segment |
 | idOfNextLowLevelRoad        | ID of the next low level road segment |
 | categoryTarget              | Default stop class (Possible values are NOT_CLASSIFIED, NON_TRAFFIC_RELATED_STOP and TRAFFIC_RELATED_STOP), must be set manually |
 