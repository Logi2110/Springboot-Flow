package com.logi.flow.resolver;

/**
 * Custom object populated and injected by RequestInfoArgumentResolver.
 *
 * Carries per-request metadata assembled from the raw HttpServletRequest.
 * Available as a method parameter in any controller that uses @InjectRequestInfo.
 */
public class RequestInfo {

    private final String requestId;
    private final String method;
    private final String uri;
    private final String remoteAddress;
    private final long receivedAt;

    public RequestInfo(String requestId, String method, String uri,
                       String remoteAddress, long receivedAt) {
        this.requestId   = requestId;
        this.method      = method;
        this.uri         = uri;
        this.remoteAddress = remoteAddress;
        this.receivedAt  = receivedAt;
    }

    public String getRequestId()      { return requestId; }
    public String getMethod()         { return method; }
    public String getUri()            { return uri; }
    public String getRemoteAddress()  { return remoteAddress; }
    public long   getReceivedAt()     { return receivedAt; }

    @Override
    public String toString() {
        return "RequestInfo{requestId='" + requestId + "', method='" + method +
               "', uri='" + uri + "', remoteAddress='" + remoteAddress + "'}";
    }
}
