package com.logi.flow.dto;

import java.util.List;

/**
 * Response DTO for GET /api/users/startup-info (Flow 5).
 *
 * Returns the data collected by StartupInfoStore during application startup:
 *   - Application metadata (name, timestamp, active profiles)
 *   - Bean definition statistics (total count, flow-package count, names)
 *   - startupSequence: ordered list of events captured by each lifecycle hook
 *     as they fired in real-time during startup
 */
public class StartupInfoResponse {

    private final String applicationName;
    private final String startupTimestamp;
    private final List<String> activeProfiles;
    private final int totalBeanCount;
    private final int flowBeanCount;
    private final List<String> flowBeanNames;
    private final List<String> startupSequence;

    public StartupInfoResponse(String applicationName, String startupTimestamp,
                               List<String> activeProfiles, int totalBeanCount,
                               int flowBeanCount, List<String> flowBeanNames,
                               List<String> startupSequence) {
        this.applicationName = applicationName;
        this.startupTimestamp = startupTimestamp;
        this.activeProfiles = activeProfiles;
        this.totalBeanCount = totalBeanCount;
        this.flowBeanCount = flowBeanCount;
        this.flowBeanNames = flowBeanNames;
        this.startupSequence = startupSequence;
    }

    public String getApplicationName()       { return applicationName; }
    public String getStartupTimestamp()      { return startupTimestamp; }
    public List<String> getActiveProfiles()  { return activeProfiles; }
    public int getTotalBeanCount()           { return totalBeanCount; }
    public int getFlowBeanCount()            { return flowBeanCount; }
    public List<String> getFlowBeanNames()   { return flowBeanNames; }
    public List<String> getStartupSequence() { return startupSequence; }
}
