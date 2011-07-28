package com.jelastic;

import com.jelastic.model.Authentication;
import com.jelastic.model.CreateObject;
import com.jelastic.model.Deploy;
import com.jelastic.model.UpLoader;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Ignore;
import org.junit.Test;

import java.net.URL;

import static org.junit.Assert.assertEquals;

/**
 * User: Igor.Yova@gmail.com
 * Date: 6/9/11
 * Time: 12:03 PM
 */

public class JsonTest {

    @Test
    public void authOkTest() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        URL url = this.getClass().getClassLoader().getResource("authentication_ok.json");
        Authentication authentication = mapper.readValue(url, Authentication.class);
        assertEquals(authentication.getSession(), "48bxaad71ccc7996325f3803311326b0247d");
    }

    @Test
    public void authErrorTest() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        URL url = this.getClass().getClassLoader().getResource("authentication_error.json");
        Authentication authentication = mapper.readValue(url, Authentication.class);
        assertEquals(authentication.getError(), "authentication failed");
    }


    @Test
    @Ignore
    public void createObjectOkTest() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        URL url = this.getClass().getClassLoader().getResource("createobject_ok.json");
        CreateObject createObject = mapper.readValue(url, CreateObject.class);
        assertEquals(createObject.getResponse().getId(), 247);
    }

    @Test
    @Ignore
    public void createObjectErrorTest() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        URL url = this.getClass().getClassLoader().getResource("createobject_error.json");
        CreateObject createObject = mapper.readValue(url, CreateObject.class);
        assertEquals(createObject.getError(), "invalid parameter [session]");
    }

    @Test
    public void upLoaderOkTest() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        URL url = this.getClass().getClassLoader().getResource("uploader_ok.json");
        UpLoader upLoader = mapper.readValue(url, UpLoader.class);
        assertEquals(upLoader.getName(), "jelastic-maven-plugin-1.0-SNAPSHOT.jar");
    }

    @Test
    public void upLoaderErrorTest() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        URL url = this.getClass().getClassLoader().getResource("uploader_error.json");
        UpLoader upLoader = mapper.readValue(url, UpLoader.class);
        assertEquals(upLoader.getError(), "invalid param");
    }

    @Test
    public void deployErrorTest() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        URL url = this.getClass().getClassLoader().getResource("deploy_error.json");
        Deploy deploy = mapper.readValue(url, Deploy.class);
        assertEquals(deploy.getError(), "application [8129583aae37a4b556d36dbd56abbc68,8129583aae37a4b556d36dbd56abbc68] not exist");
    }

    @Test
    public void deployOkTest() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        URL url = this.getClass().getClassLoader().getResource("deploy_ok.json");
        Deploy deploy = mapper.readValue(url, Deploy.class);
        assertEquals(deploy.getResponse().getResult(), 0);
    }

    @Test
    public void deploy() throws Exception {
        String fff = "miltrex-web-1.0.0.war";
        System.out.println(fff.substring(0,fff.length()-4));
    }

}
