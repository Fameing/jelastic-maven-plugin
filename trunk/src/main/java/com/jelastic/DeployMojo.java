package com.jelastic;

import com.jelastic.model.*;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Goal which touches a timestamp file.
 *
 * @goal deploy
 * @phase install
 */
public class DeployMojo extends JelasticMojo {

    public void execute() throws MojoExecutionException, MojoFailureException {
        Authentication authentication = authentication();
        if (authentication.getResult() == 0) {
            getLog().info("------------------------------------------------------------------------");
            getLog().info("   Authentication : SUCCESS");
            getLog().info("          Session : " + authentication.getSession());
            //getLog().info("              Uid : " + authentication.getUid());
            getLog().info("------------------------------------------------------------------------");
            UpLoader upLoader = upload(authentication);
            if (upLoader.getResult() == 0) {
                getLog().info("      File UpLoad : SUCCESS");
                getLog().info("         File URL : " + upLoader.getFile());
                getLog().info("        File size : " + upLoader.getSize());
                getLog().info("------------------------------------------------------------------------");
                CreateObject createObject = createObject(upLoader, authentication);
                if (createObject.getResult() == 0) {
                    getLog().info("File registration : SUCCESS");
                    getLog().info("  Registration ID : " + createObject.getResponse().getObject().getId());
                    getLog().info("     Developer ID : " + createObject.getResponse().getObject().getDeveloper());
                    getLog().info("------------------------------------------------------------------------");
                    if (isUploadOnly()) return;
                    Deploy deploy = deploy(authentication, upLoader, createObject);
                    if (deploy.getResponse().getResult() == 0) {
                        getLog().info("      Deploy file : SUCCESS");
                        getLog().info("       Deploy log :");
                        getLog().info(deploy.getResponse().getResponses()[0].getOut());
                        LogOut logOut = logOut(authentication);
                        if (logOut.getResult() == 0) {
                            getLog().info("           LogOut : SUCCESS");
                        } else {
                            getLog().info("LogOut : FAILED");
                            getLog().error("Error : " + logOut.getError());
                        }
                    } else {
                        getLog().error("          Deploy : FAILED");
                        getLog().error("           Error : " + deploy.getResponse().getError());
                    }
                }
            } else {
                getLog().error("File upload : FAILED");
                getLog().error("      Error : " + upLoader.getError());
            }
        } else {
            getLog().error("Authentication : FAILED");
            getLog().error("         Error : " + authentication.getError());
        }


    }

}
