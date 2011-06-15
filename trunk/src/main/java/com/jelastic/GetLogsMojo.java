package com.jelastic;

import com.jelastic.model.Authentication;
import com.jelastic.model.LogsResponse;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

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
            LogsResponse logsResponse = getJelasticLogs(authentication);
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
