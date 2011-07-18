package com.jelastic;

/**
 * User: Igor.Yova@gmail.com
 * Date: 6/8/11
 * Time: 10:30 AM
 */

import com.jelastic.model.*;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public abstract class JelasticMojo extends AbstractMojo {
    private String shema = "http";
    private String hostApp = "app.hivext.com";
    private String hostApi = "api.hivext.com";
    private String hostApiDe = "de-hetzner.hivext.com";
    private String hostLogs = "178.159.250.35";

    private int port = -1;
    private String version = "1.0";

    private CookieStore cookieStore = null;
    private String urlAuthentication = "/" + version + "/users/authentication/rest/signin";
    private String urlUploader = "/" + version + "/storage/uploader/rest/upload";
    private String urlCreateObject = "/" + version + "/data/base/rest/createobject";
    private String urlGetLogs = "/JElastic/dev/env/rest/getlogs";
    //private String urlEval = "/" + version + "/dev/scripting/rest/eval";
    private String urlEval = "/" + version + "/development/scripting/rest/eval";
    private static ObjectMapper mapper = new ObjectMapper();


    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */

    private MavenProject project;


    /**
     * System AppID Properties.
     *
     * @parameter default-value="8129583aae37a4b556d36dbd56abbc68"
     */
    private String jelastiсAppid;


/*
  *  *//**
     * AppID Properties.
     *
     * @parameter
     * @required
     *//*
    private String appid;
    */

    /**
     * Email Properties.
     *
     * @parameter
     */
    private String email;

    /**
     * Password Properties.
     *
     * @parameter
     * @required
     */
    private String password;

    /**
     * Context Properties.
     *
     * @parameter default-value="ROOT"
     */
    private String context;


    /**
     * Environment name Properties.
     *
     * @parameter
     */
    private String environment;

    /**
     * War finalName Properties.
     *
     * @parameter expression="${project.build.finalName}" default-value="${project.build.finalName}"
     */
    private String finalName;

    /**
     * Location of the file.
     *
     * @parameter expression="${project.build.directory}" default-value="${project.build.directory}"
     * @required
     */
    public File outputDirectory;

    public String getFinalName() {
        return finalName;
    }

    public File getOutputDirectory() {
        return outputDirectory;
    }

    public String getShema() {
        return shema;
    }

    public String getHostApp() {
        return hostApp;
    }

    public String getHostApi() {
        return hostApi;
    }

    public String getHostLogs() {
        return hostLogs;
    }

    public String getHostApiDe() {
        return hostApiDe;
    }

    public int getPort() {
        return port;
    }

    public CookieStore getCookieStore() {
        return cookieStore;
    }

    public String getUrlAuthentication() {
        return urlAuthentication;
    }

    public String getUrlUploader() {
        return urlUploader;
    }

    public String getUrlCreateObject() {
        return urlCreateObject;
    }

    public String getUrlGetLogs() {
        return urlGetLogs;
    }

    public String getUrlEval() {
        return urlEval;
    }

    public String getJelastiсAppid() {
        return jelastiсAppid;
    }

/*    public String getAppid() {
        return appid;
    }*/

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getContext() {
        return context;
    }

    public String getEnvironment() {
        return environment;
    }

    private boolean isValidPackaging() {
        return project.getModel().getPackaging().equals("war");
    }

    public Authentication authentication() throws MojoExecutionException {
        boolean validate = isValidPackaging();
        if (!validate) {
            throw new MojoExecutionException("Packaging is not 'war'");
        }


        Authentication authentication = null;

        try {
            DefaultHttpClient httpclient = new DefaultHttpClient();

            List<NameValuePair> qparams = new ArrayList<NameValuePair>();
            qparams.add(new BasicNameValuePair("appid", getJelastiсAppid()));
            qparams.add(new BasicNameValuePair("login", getEmail()));
            qparams.add(new BasicNameValuePair("password", getPassword()));
            URI uri = URIUtils.createURI(getShema(), getHostApp(), getPort(), getUrlAuthentication(), URLEncodedUtils.format(qparams, "UTF-8"), null);
            getLog().debug(uri.toString());
            HttpGet httpGet = new HttpGet(uri);
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            String responseBody = httpclient.execute(httpGet, responseHandler);
            getLog().debug(responseBody);
            authentication = mapper.readValue(responseBody, Authentication.class);
            cookieStore = httpclient.getCookieStore();
        } catch (URISyntaxException e) {
            getLog().error(e.getMessage(), e);
        } catch (ClientProtocolException e) {
            getLog().error(e.getMessage(), e);
        } catch (IOException e) {
            getLog().error(e.getMessage(), e);
        }
        return authentication;
    }

    public UpLoader upload(Authentication authentication) throws MojoExecutionException {
        UpLoader upLoader = null;
        try {
            DefaultHttpClient httpclient = new DefaultHttpClient();
            httpclient.setCookieStore(getCookieStore());

            File file = new File(getOutputDirectory() + File.separator + getFinalName() + "." + project.getModel().getPackaging());
            if (!file.exists()) {
                throw new MojoExecutionException("First build artifact and try again. Artifact not found " + getFinalName() + "." + project.getModel().getPackaging());
            }

            MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            multipartEntity.addPart("appid", new StringBody(getJelastiсAppid()));
            multipartEntity.addPart("fid", new StringBody("123456"));
            multipartEntity.addPart("session", new StringBody(authentication.getSession()));
            multipartEntity.addPart("file", new FileBody(file));


            URI uri = URIUtils.createURI(getShema(), getHostApi(), getPort(), getUrlUploader(), null, null);
            getLog().debug(uri.toString());
            HttpPost httpPost = new HttpPost(uri);
            httpPost.setEntity(multipartEntity);

            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            String responseBody = httpclient.execute(httpPost, responseHandler);
            getLog().debug(responseBody);
            upLoader = mapper.readValue(responseBody, UpLoader.class);
        } catch (URISyntaxException e) {
            getLog().error(e.getMessage(), e);
        } catch (ClientProtocolException e) {
            getLog().error(e.getMessage(), e);
        } catch (IOException e) {
            getLog().error(e.getMessage(), e);
        }
        return upLoader;
    }

    public CreateObject createObject(UpLoader upLoader, Authentication authentication) {
        CreateObject createObject = null;
        try {
            DefaultHttpClient httpclient = new DefaultHttpClient();
            httpclient.setCookieStore(getCookieStore());

            List<NameValuePair> nameValuePairList = new ArrayList<NameValuePair>();
            nameValuePairList.add(new BasicNameValuePair("charset", "UTF-8"));
            nameValuePairList.add(new BasicNameValuePair("appid", getJelastiсAppid()));
            nameValuePairList.add(new BasicNameValuePair("session", authentication.getSession()));
            nameValuePairList.add(new BasicNameValuePair("type", "JDeploy"));
            nameValuePairList.add(new BasicNameValuePair("data", "{'name':'" + getFinalName() + "." + project.getModel().getPackaging() + "', 'archive':'" + upLoader.getFile() + "', 'link':0, 'size':" + upLoader.getSize() + ", 'comment':'" + getFinalName() + "." + project.getModel().getPackaging() + "'}"));

            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(nameValuePairList, "UTF-8");
            URI uri = URIUtils.createURI(getShema(), getHostApp(), getPort(), getUrlCreateObject(), null, null);
            getLog().debug(uri.toString());
            HttpPost httpPost = new HttpPost(uri);
            httpPost.setEntity(entity);

            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            String responseBody = httpclient.execute(httpPost, responseHandler);
            getLog().debug(responseBody);
            createObject = mapper.readValue(responseBody, CreateObject.class);
        } catch (URISyntaxException e) {
            getLog().error(e.getMessage(), e);
        } catch (ClientProtocolException e) {
            getLog().error(e.getMessage(), e);
        } catch (IOException e) {
            getLog().error(e.getMessage(), e);
        }
        return createObject;
    }

    public Deploy deploy(Authentication authentication, UpLoader upLoader, CreateObject createObject) {
        Deploy deploy = null;

        try {
            DefaultHttpClient httpclient = new DefaultHttpClient();
            httpclient.setCookieStore(getCookieStore());

            List<NameValuePair> qparams = new ArrayList<NameValuePair>();
            qparams.add(new BasicNameValuePair("charset", "UTF-8"));
            qparams.add(new BasicNameValuePair("appid", getJelastiсAppid()));
            qparams.add(new BasicNameValuePair("session", authentication.getSession()));
            qparams.add(new BasicNameValuePair("script", "deploy/DeployArchive"));
            qparams.add(new BasicNameValuePair("archiveUri", upLoader.getFile()));
            qparams.add(new BasicNameValuePair("archiveName", upLoader.getName()));
            qparams.add(new BasicNameValuePair("newContext", getContext()));
            qparams.add(new BasicNameValuePair("domain", getEnvironment()));

            URI uri = URIUtils.createURI(getShema(), getHostApiDe(), getPort(), getUrlEval(), URLEncodedUtils.format(qparams, "UTF-8"), null);
            getLog().debug(uri.toString());
            HttpGet httpPost = new HttpGet(uri);
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            String responseBody = httpclient.execute(httpPost, responseHandler);
            getLog().debug(responseBody);
            deploy = mapper.readValue(responseBody, Deploy.class);
        } catch (URISyntaxException e) {
            getLog().error(e.getMessage(), e);
        } catch (ClientProtocolException e) {
            getLog().error(e.getMessage(), e);
        } catch (IOException e) {
            getLog().error(e.getMessage(), e);
        }
        return deploy;
    }

    public LogsResponse getJelasticLogs(Authentication authentication) {
        LogsResponse logsResponse = null;
        try {
            DefaultHttpClient httpclient = new DefaultHttpClient();
            httpclient.setCookieStore(getCookieStore());

            List<NameValuePair> qparams = new ArrayList<NameValuePair>();
            qparams.add(new BasicNameValuePair("charset", "UTF-8"));
            //qparams.add(new BasicNameValuePair("appid", getAppid()));
            qparams.add(new BasicNameValuePair("session", authentication.getSession()));
            qparams.add(new BasicNameValuePair("path", "logs"));
            qparams.add(new BasicNameValuePair("count", "250"));

            URI uri = URIUtils.createURI(getShema(), getHostLogs(), 8080, getUrlGetLogs(), URLEncodedUtils.format(qparams, "UTF-8"), null);
            getLog().debug(uri.toString());
            HttpPost httpPost = new HttpPost(uri);
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            String responseBody = httpclient.execute(httpPost, responseHandler);
            getLog().debug(responseBody);
            logsResponse = mapper.readValue(responseBody, LogsResponse.class);
        } catch (URISyntaxException e) {
            getLog().error(e.getMessage(), e);
        } catch (ClientProtocolException e) {
            getLog().error(e.getMessage(), e);
        } catch (IOException e) {
            getLog().error(e.getMessage(), e);
        }
        return logsResponse;
    }

}
