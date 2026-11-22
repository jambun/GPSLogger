package eu.basicairdata.graziano.gpslogger;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import fi.iki.elonen.NanoHTTPD;
import com.google.gson.Gson;

public class TrackHttpServer extends NanoHTTPD {

    public TrackHttpServer(int port) throws IOException {
        super(port);
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        Method method = session.getMethod();
        Map<String, List<String>> params = session.getParameters();

        if (Method.GET.equals(method) && "/locations".equals(uri)) {
            try {
                Track track = GPSApplication.getInstance().getCurrentTrack();

                if (track == null) {
                    return addHeaders(newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "No current track"));
                }

                if (track.getNumberOfLocations() == 0) {
                    return addHeaders(newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Current track has no locations"));
                }

                long since = Long.parseLong(params.get("since").get(0));

                List<LocationExtended> lList = new ArrayList<>();

                lList.addAll(GPSApplication.getInstance().gpsDataBase.getLocationsSince(track.getId(), since));

                if (lList.isEmpty()) {
                    return addHeaders(newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Current track locations since " + since));
                }

                List<Map> outList = new ArrayList<>();

                for (LocationExtended loc : lList) {
                    Map<String, Number> locMap = new HashMap<>();
                    locMap.put("lat", loc.getLatitude());
                    locMap.put("lon", loc.getLongitude());
                    locMap.put("ele", loc.getAltitude());
                    locMap.put("time", loc.getTime());
                    outList.add(locMap);
                }

                Gson gson = new Gson();
                String json = gson.toJson(outList);

                return addHeaders(newFixedLengthResponse(Response.Status.OK, "application/json", json));

            } catch (NumberFormatException e) {
                return addHeaders(newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/plain", "Bad since param - must be a long integer"));
            }
        }


        if (Method.GET.equals(method) && "/".equals(uri)) {
            String json = "{\"status\": \"" + uri + "\"}";
            return addHeaders(newFixedLengthResponse(Response.Status.OK, "application/json", json));
        }

        // probably not needed, but it was hard won so leaving this relic for now
        // if (Method.OPTIONS.equals(method) && "/status".equals(uri)) {
        //     Response response = newFixedLengthResponse(Response.Status.NO_CONTENT, NanoHTTPD.MIME_PLAINTEXT, "");
        //     response.addHeader("Access-Control-Allow-Origin", "https://hudmol.com");
        //     response.addHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        //     response.addHeader("Access-Control-Allow-Private-Network", "true");
        //     response.addHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        //     response.addHeader("Access-Control-Max-Age", "86400");

        //     return response;
        // }

        if (Method.GET.equals(method) && "/status".equals(uri)) {
            Track track = GPSApplication.getInstance().getCurrentTrack();
            String json = "{\"status\": \"" + track.getNumberOfLocations() + "\"}";

            return addHeaders(newFixedLengthResponse(Response.Status.OK, "application/json", json));
        }

        return addHeaders(newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Unknown URI"));
    }

    private Response addHeaders(Response response) {
        response.addHeader("Access-Control-Allow-Origin", "https://hudmol.com");
        response.addHeader("Access-Control-Allow-Private-Network", "true");
        response.addHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        return response;
    }
}
