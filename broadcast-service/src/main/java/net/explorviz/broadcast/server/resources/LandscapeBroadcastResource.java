package net.explorviz.broadcast.server.resources;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.sse.SseEventSink;
import net.explorviz.broadcast.server.helper.LandscapeBroadcastService;

/**
 * Resource class that contains an endpoint which clients, e.g., the ExplorViz Frontend, can use to
 * registered to landscape updates.
 *
 */
@Path("v1/landscapes/broadcast")
@RolesAllowed({"admin"})
public class LandscapeBroadcastResource {

  private final LandscapeBroadcastService broadcastService;

  @Inject
  public LandscapeBroadcastResource(final LandscapeBroadcastService broadcastService) {
    this.broadcastService = broadcastService;
  }

  // curl -v -X GET http://localhost:8081/v1/landscapes/broadcast/ -H
  // "Content-Type:
  // text/event-stream"

  /**
   * Endpoint that clients can use to register for landscape updates.
   *
   * @param eventSink - The to-be registered event sink.
   * @param response - {@link HttpServletResponse} which is enriched with header information.
   */
  @GET
  @Produces(MediaType.SERVER_SENT_EVENTS)
  public void listenToBroadcast(@Context final SseEventSink eventSink,
      @Context final HttpServletResponse response) {

    // https://serverfault.com/a/801629
    response.addHeader("Cache-Control", "no-cache");
    response.addHeader("X-Accel-Buffering", "no");

    this.broadcastService.register(eventSink);
  }
}