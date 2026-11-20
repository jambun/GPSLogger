package eu.basicairdata.graziano.gpslogger;

import java.io.IOException;
import java.util.Map;
import fi.iki.elonen.NanoHTTPD;

public class TrackHttpServer extends NanoHTTPD {

    public TrackHttpServer(int port) throws IOException {
        super(port);
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        Method method = session.getMethod();

        if (Method.GET.equals(method) && "/".equals(uri)) {
            String html = "<html><body><h1>Hello from Android!</h1></body></html>";
            return newFixedLengthResponse(Response.Status.OK, "text/html", html);
        }

        if (Method.GET.equals(method) && "/status".equals(uri)) {
            String json = "{\"status\": \"running\"}";
            return newFixedLengthResponse(Response.Status.OK, "application/json", json);
        }

        return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "404 Not Found");
    }
}
