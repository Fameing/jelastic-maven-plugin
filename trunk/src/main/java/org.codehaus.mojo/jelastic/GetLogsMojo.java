package org.codehaus.mojo.jelastic;

import org.codehaus.mojo.jelastic.model.Authentication;
import org.codehaus.mojo.jelastic.model.LogsResponse;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * User: Igor.Yova@gmail.com
 * Date: 6/9/11
 * Time: 5:48 PM
 */

/**
 * Goal which touches a timestamp file.
 *
 */
 //* @goal getlogs
 //* @phase process-sources
public class GetLogsMojo extends JelasticMojo {
    public void execute() throws MojoExecutionException, MojoFailureException {
        Authentication authentication = authentication();
        if (authentication.getResult() == 0) {
            getLog().info("------------------------------------------------------------------------");
            getLog().info("   Authentication : SUCCESS");
            getLog().info("          Session : " + authentication.getSession());
            getLog().info("              Uid : " + authentication.getUid());
            getLog().info("------------------------------------------------------------------------");
           //TODO
            LogsResponse logsResponse = null;//getJelasticLogs(authentication);
            if (logsResponse.getResult() == 0) {
                getLog().info("         Get logs : SUCCESS");
                getLog().info("      Lines Count : " + logsResponse.getLinescount());
                getLog().info("------------------------------------------------------------------------");
                getLog().info(logsResponse.getBody());
            } else {
            getLog().error("Get logs : FAILED");
            getLog().error("   Error : " + logsResponse.getError());
            }
        } else {
            getLog().error("Authentication : FAILED");
            getLog().error("         Error : " + authentication.getError());
        }
    }
}
