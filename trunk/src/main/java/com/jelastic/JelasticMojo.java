package com.jelastic;

/**
 * User: Igor.Yova@gmail.com
 * Date: 6/8/11
 * Time: 10:30 AM
 */


/**
 *        http://app.hivext.com/1.0/users/authentication/rest/signin
 *        http://api.hivext.com/1.0/storage/uploader/rest/upload
 *        http://app.hivext.com/1.0/data/base/rest/createobject
 *        http://live.jelastic.com/deploy/DeployArchive
 */

import com.jelastic.model.*;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.message.AbstractHttpMessage;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Proxy;
import org.codehaus.jackson.map.ObjectMapper;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Map;
import java.net.URLDecoder;

public abstract class JelasticMojo extends AbstractMojo {
    private String shema = "https";
    private int port = -1;
    private String version = "1.0";
    private long totalSize;
    private int numSt;
    private CookieStore cookieStore = null;
    private String urlAuthentication = "/" + version + "/users/authentication/rest/signin";
    private String urlUploader = "/" + version + "/storage/uploader/rest/upload";
    private String urlCreateObject = "/deploy/createobject";
    private String urlDeploy = "/deploy/DeployArchive";
    private String urlLogOut = "/users/authentication/rest/signout";
    private static ObjectMapper mapper = new ObjectMapper();
    private static Properties properties = new Properties();


    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */

    private MavenProject project;

    /**
     * The Maven session.
     *
     * @parameter expression="${session}"
     * @readonly
     * @required
     */
    private MavenSession mavenSession;

    /**
     * Headers Properties.
     *
     * @parameter
     */
    private Map<String, String> headers;

    /**
     * Email Properties.
     *
     * @parameter
     */
    private String email;

    /**
     * Comment Properties.
     *
     * @parameter
     */
    private String comment;


    /**
     * Password Properties.
     *
     * @parameter
     */
    private String password;

    /**
     * Context Properties.
     *
     * @parameter default-value="ROOT"
     */
    private String context;


    /**
     * Context Properties.
     *
     * @parameter default-value="api.jelastic.com"
     */
    private String api_hoster;


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

    public String getApiJelastic() {
        if (System.getProperty("jelastic-hoster") != null && System.getProperty("jelastic-hoster").length() > 0) {
            api_hoster = System.getProperty("jelastic-hoster");
        }
        return api_hoster;
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

    public String getUrlDeploy() {
        return urlDeploy;
    }

    public String getUrlLogOut() {
        return urlLogOut;
    }

    public String getEmail() {
        if (isExternalParameterPassed()) {
            if (properties.getProperty("jelastic-email") != null && properties.getProperty("jelastic-email").length() > 0) {
                return properties.getProperty("jelastic-email");
            } else {
                return email;
            }
        } else {
            return email;
        }
    }

    public String getPassword() {
        if (isExternalParameterPassed()) {
            if (properties.getProperty("jelastic-password") != null && properties.getProperty("jelastic-password").length() > 0) {
                return properties.getProperty("jelastic-password");
            } else {
                return password;
            }
        } else {
            return password;
        }
    }

    public String getContext() {
        if (isExternalParameterPassed()) {
            if (properties.getProperty("context") != null && properties.getProperty("context").length() > 0) {
                return properties.getProperty("context");
            } else {
                return context;
            }
        } else {
            return context;
        }
    }

    public String getEnvironment() {
        if (isExternalParameterPassed()) {
            if (properties.getProperty("environment") != null && properties.getProperty("environment").length() > 0) {
                return properties.getProperty("environment");
            } else {
                return environment;
            }
        } else {
            return environment;
        }
    }

    public boolean isExternalParameterPassed() {
        if (System.getProperty("jelastic-properties") != null && System.getProperty("jelastic-properties").length() > 0) {
            try {
                properties.load(new FileInputStream(System.getProperty("jelastic-properties")));
            } catch (IOException e) {
                getLog().error(e.getMessage(), e);
                return false;
            }
        } else {
            return false;
        }
        return true;
    }

    public boolean isUploadOnly() {
        String uploadOnly = System.getProperty("jelastic-upload-only");
        return uploadOnly != null && (uploadOnly.equalsIgnoreCase("1") || uploadOnly.equalsIgnoreCase("true"));
    }

    public Authentication authentication() throws MojoExecutionException {
        Authentication authentication = new Authentication();
        String jelasticHeaders = System.getProperty("jelastic-headers");
        getLog().debug("jelastic-headers=" + jelasticHeaders);
        if (jelasticHeaders != null && jelasticHeaders.length() > 0) {
            try {
                headers = mapper.readValue(URLDecoder.decode(jelasticHeaders, "UTF8"), Map.class);
                getLog().debug("headers=" + headers);
            } catch (IOException e) {
                getLog().error(e.getMessage(), e);
            }
        }
        UsernamePasswordCredentials usernamePasswordCredentials = null;
        if (System.getProperty("jelastic-session") != null && System.getProperty("jelastic-session").length() > 0) {
            authentication.setSession(System.getProperty("jelastic-session"));
            authentication.setResult(0);
        } else {
            List<Proxy> proxyList = mavenSession.getSettings().getProxies();
            HttpHost http_proxy = null;
            for (Proxy proxy : proxyList) {
                if (proxy.getProtocol().equalsIgnoreCase("http") || proxy.isActive()) {
                    http_proxy = new HttpHost(proxy.getHost(), proxy.getPort(), proxy.getProtocol());
                    if (proxy.getUsername() != null || proxy.getPassword() != null) {
                        usernamePasswordCredentials = new UsernamePasswordCredentials(proxy.getUsername(), proxy.getPassword());
                    }
                } else if (proxy.getProtocol().equalsIgnoreCase("https") || proxy.isActive()) {
                    http_proxy = new HttpHost(proxy.getHost(), proxy.getPort(), proxy.getProtocol());
                    if (proxy.getUsername() != null || proxy.getPassword() != null) {
                        usernamePasswordCredentials = new UsernamePasswordCredentials(proxy.getUsername(), proxy.getPassword());
                    }
                }
            }
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                httpclient = wrapClient(httpclient);
                if (http_proxy != null) {
                    httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, http_proxy);
                    if (usernamePasswordCredentials != null) {
                        httpclient.getCredentialsProvider().setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT), usernamePasswordCredentials);
                    }
                }
                httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, http_proxy);
                List<NameValuePair> qparams = new ArrayList<NameValuePair>();
                qparams.add(new BasicNameValuePair("login", getEmail()));
                qparams.add(new BasicNameValuePair("password", getPassword()));
                URI uri = URIUtils.createURI(getShema(), getApiJelastic(), getPort(), getUrlAuthentication(), URLEncodedUtils.format(qparams, "UTF-8"), null);
                getLog().debug(uri.toString());
                HttpGet httpGet = new HttpGet(uri);
                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                String responseBody = httpclient.execute(httpGet, responseHandler);
                cookieStore = httpclient.getCookieStore();

                List<Cookie> cookies = cookieStore.getCookies();
                for (Cookie cookie : cookies) {
                    getLog().debug(cookie.getName() + " = " + cookie.getValue());
                }

                getLog().debug(responseBody);
                authentication = mapper.readValue(responseBody, Authentication.class);
            } catch (URISyntaxException e) {
                getLog().error(e.getMessage(), e);
            } catch (ClientProtocolException e) {
                getLog().error(e.getMessage(), e);
            } catch (IOException e) {
                getLog().error(e.getMessage(), e);
            }
        }
        return authentication;
    }

    public UpLoader upload(Authentication authentication) throws MojoExecutionException {
        UpLoader upLoader = null;
        List<Proxy> proxyList = mavenSession.getSettings().getProxies();
        HttpHost http_proxy = null;
        UsernamePasswordCredentials usernamePasswordCredentials = null;
        for (Proxy proxy : proxyList) {
            if (proxy.getProtocol().equalsIgnoreCase("http") || proxy.isActive()) {
                http_proxy = new HttpHost(proxy.getHost(), proxy.getPort(), proxy.getProtocol());
                if (proxy.getUsername() != null || proxy.getPassword() != null) {
                    usernamePasswordCredentials = new UsernamePasswordCredentials(proxy.getUsername(), proxy.getPassword());
                }
            } else if (proxy.getProtocol().equalsIgnoreCase("https") || proxy.isActive()) {
                http_proxy = new HttpHost(proxy.getHost(), proxy.getPort(), proxy.getProtocol());
                if (proxy.getUsername() != null || proxy.getPassword() != null) {
                    usernamePasswordCredentials = new UsernamePasswordCredentials(proxy.getUsername(), proxy.getPassword());
                }
            }
        }
        try {
            DefaultHttpClient httpclient = new DefaultHttpClient();
            httpclient = wrapClient(httpclient);
            if (http_proxy != null) {
                httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, http_proxy);
                if (usernamePasswordCredentials != null) {
                    httpclient.getCredentialsProvider().setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT), usernamePasswordCredentials);
                }
            }
            httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, http_proxy);
            httpclient.setCookieStore(getCookieStore());
            for (Cookie cookie : httpclient.getCookieStore().getCookies()) {
                getLog().debug(cookie.getName() + " = " + cookie.getValue());
            }
            final File file = new File(getOutputDirectory() + File.separator + getFinalName() + "." + project.getArtifact().getArtifactHandler().getExtension());
            if (!file.exists()) {
                throw new MojoExecutionException("First build artifact and try again. Artifact not found " + getFinalName() + "." + project.getArtifact().getArtifactHandler().getExtension());
            }


            getLog().info("File Uploading Progress :");
            CustomMultiPartEntity multipartEntity = new CustomMultiPartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, new CustomMultiPartEntity.ProgressListener() {
                public void transferred(long num) {
                    if (((int) ((num / (float) totalSize) * 100)) != numSt) {
                        getLog().info("[" + (int) ((num / (float) totalSize) * 100) + "%]");
                        numSt = ((int) ((num / (float) totalSize) * 100));
                    }
                }
            });

            multipartEntity.addPart("fid", new StringBody("123456"));
            multipartEntity.addPart("session", new StringBody(authentication.getSession()));
            multipartEntity.addPart("file", new FileBody(file));
            totalSize = multipartEntity.getContentLength();

            URI uri = URIUtils.createURI(getShema(), getApiJelastic(), getPort(), getUrlUploader(), null, null);
            getLog().debug(uri.toString());
            HttpPost httpPost = new HttpPost(uri);
            addHeaders(httpPost);
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
        List<Proxy> proxyList = mavenSession.getSettings().getProxies();
        HttpHost http_proxy = null;
        UsernamePasswordCredentials usernamePasswordCredentials = null;
        for (Proxy proxy : proxyList) {
            if (proxy.getProtocol().equalsIgnoreCase("http") || proxy.isActive()) {
                http_proxy = new HttpHost(proxy.getHost(), proxy.getPort(), proxy.getProtocol());
                if (proxy.getUsername() != null || proxy.getPassword() != null) {
                    usernamePasswordCredentials = new UsernamePasswordCredentials(proxy.getUsername(), proxy.getPassword());
                }
            } else if (proxy.getProtocol().equalsIgnoreCase("https") || proxy.isActive()) {
                http_proxy = new HttpHost(proxy.getHost(), proxy.getPort(), proxy.getProtocol());
                if (proxy.getUsername() != null || proxy.getPassword() != null) {
                    usernamePasswordCredentials = new UsernamePasswordCredentials(proxy.getUsername(), proxy.getPassword());
                }
            }
        }
        try {
            DefaultHttpClient httpclient = new DefaultHttpClient();
            httpclient = wrapClient(httpclient);
            if (http_proxy != null) {
                httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, http_proxy);
                if (usernamePasswordCredentials != null) {
                    httpclient.getCredentialsProvider().setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT), usernamePasswordCredentials);
                }
            }
            httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, http_proxy);
            httpclient.setCookieStore(getCookieStore());
            for (Cookie cookie : httpclient.getCookieStore().getCookies()) {
                getLog().debug(cookie.getName() + " = " + cookie.getValue());
            }

            String local_comment = "";
            if (System.getProperty("jelastic-comment") != null && System.getProperty("jelastic-comment").length() > 0) {
                local_comment = System.getProperty("jelastic-comment");
            } else if (comment != null && !comment.isEmpty()) {
                local_comment = comment;
            } else {
                local_comment = getFinalName() + "." + project.getModel().getPackaging();
            }

            List<NameValuePair> nameValuePairList = new ArrayList<NameValuePair>();
            nameValuePairList.add(new BasicNameValuePair("charset", "UTF-8"));
            nameValuePairList.add(new BasicNameValuePair("session", authentication.getSession()));
            nameValuePairList.add(new BasicNameValuePair("type", "JDeploy"));
            nameValuePairList.add(new BasicNameValuePair("data", "{'name':'" + getFinalName() + "." + project.getModel().getPackaging() + "', 'archive':'" + upLoader.getFile() + "', 'link':0, 'size':" + upLoader.getSize() + ", 'comment':'" + local_comment + "'}"));

            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(nameValuePairList, "UTF-8");

            for (NameValuePair nameValuePair : nameValuePairList) {
                getLog().debug(nameValuePair.getName() + " : " + nameValuePair.getValue());
            }

            URI uri = URIUtils.createURI(getShema(), getApiJelastic(), getPort(), getUrlCreateObject(), null, null);
            getLog().debug(uri.toString());
            HttpPost httpPost = new HttpPost(uri);
            addHeaders(httpPost);
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
        List<Proxy> proxyList = mavenSession.getSettings().getProxies();
        HttpHost http_proxy = null;
        UsernamePasswordCredentials usernamePasswordCredentials = null;
        for (Proxy proxy : proxyList) {
            if (proxy.getProtocol().equalsIgnoreCase("http") || proxy.isActive()) {
                http_proxy = new HttpHost(proxy.getHost(), proxy.getPort(), proxy.getProtocol());
                if (proxy.getUsername() != null || proxy.getPassword() != null) {
                    usernamePasswordCredentials = new UsernamePasswordCredentials(proxy.getUsername(), proxy.getPassword());
                }
            } else if (proxy.getProtocol().equalsIgnoreCase("https") || proxy.isActive()) {
                http_proxy = new HttpHost(proxy.getHost(), proxy.getPort(), proxy.getProtocol());
                if (proxy.getUsername() != null || proxy.getPassword() != null) {
                    usernamePasswordCredentials = new UsernamePasswordCredentials(proxy.getUsername(), proxy.getPassword());
                }
            }
        }
        try {
            DefaultHttpClient httpclient = new DefaultHttpClient();
            httpclient = wrapClient(httpclient);
            if (http_proxy != null) {
                httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, http_proxy);
                if (usernamePasswordCredentials != null) {
                    httpclient.getCredentialsProvider().setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT), usernamePasswordCredentials);
                }
            }

            httpclient.setCookieStore(getCookieStore());

            for (Cookie cookie : httpclient.getCookieStore().getCookies()) {
                getLog().debug(cookie.getName() + " = " + cookie.getValue());
            }


            List<NameValuePair> qparams = new ArrayList<NameValuePair>();
            qparams.add(new BasicNameValuePair("charset", "UTF-8"));
            qparams.add(new BasicNameValuePair("session", authentication.getSession()));
            qparams.add(new BasicNameValuePair("archiveUri", upLoader.getFile()));
            qparams.add(new BasicNameValuePair("archiveName", upLoader.getName()));
            qparams.add(new BasicNameValuePair("newContext", getContext()));
            qparams.add(new BasicNameValuePair("domain", getEnvironment()));

            URI uri = URIUtils.createURI(getShema(), getApiJelastic(), getPort(), getUrlDeploy(), URLEncodedUtils.format(qparams, "UTF-8"), null);
            getLog().debug(uri.toString());
            HttpGet httpPost = new HttpGet(uri);
            addHeaders(httpPost);
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


    public LogOut logOut(Authentication authentication) {
        LogOut logOut = null;
        List<Proxy> proxyList = mavenSession.getSettings().getProxies();
        HttpHost http_proxy = null;
        UsernamePasswordCredentials usernamePasswordCredentials = null;
        for (Proxy proxy : proxyList) {
            if (proxy.getProtocol().equalsIgnoreCase("http") || proxy.isActive()) {
                http_proxy = new HttpHost(proxy.getHost(), proxy.getPort(), proxy.getProtocol());
                if (proxy.getUsername() != null || proxy.getPassword() != null) {
                    usernamePasswordCredentials = new UsernamePasswordCredentials(proxy.getUsername(), proxy.getPassword());
                }
            } else if (proxy.getProtocol().equalsIgnoreCase("https") || proxy.isActive()) {
                http_proxy = new HttpHost(proxy.getHost(), proxy.getPort(), proxy.getProtocol());
                if (proxy.getUsername() != null || proxy.getPassword() != null) {
                    usernamePasswordCredentials = new UsernamePasswordCredentials(proxy.getUsername(), proxy.getPassword());
                }
            }
        }
        try {
            DefaultHttpClient httpclient = new DefaultHttpClient();
            httpclient = wrapClient(httpclient);
            if (http_proxy != null) {
                httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, http_proxy);
                if (usernamePasswordCredentials != null) {
                    httpclient.getCredentialsProvider().setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT), usernamePasswordCredentials);
                }
            }

            httpclient.setCookieStore(getCookieStore());

            for (Cookie cookie : httpclient.getCookieStore().getCookies()) {
                getLog().debug(cookie.getName() + " = " + cookie.getValue());
            }


            List<NameValuePair> qparams = new ArrayList<NameValuePair>();
            qparams.add(new BasicNameValuePair("charset", "UTF-8"));
            qparams.add(new BasicNameValuePair("session", authentication.getSession()));

            URI uri = URIUtils.createURI(getShema(), getApiJelastic(), getPort(), getUrlLogOut(), URLEncodedUtils.format(qparams, "UTF-8"), null);
            getLog().debug(uri.toString());
            HttpGet httpPost = new HttpGet(uri);
            addHeaders(httpPost);
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            String responseBody = httpclient.execute(httpPost, responseHandler);
            getLog().debug(responseBody);
            logOut = mapper.readValue(responseBody, LogOut.class);
        } catch (URISyntaxException e) {
            getLog().error(e.getMessage(), e);
        } catch (ClientProtocolException e) {
            getLog().error(e.getMessage(), e);
        } catch (IOException e) {
            getLog().error(e.getMessage(), e);
        }
        return logOut;
    }


    private void addHeaders(AbstractHttpMessage message) {
        if (headers != null) {
            for (String key : headers.keySet()) {
                String value = headers.get(key);
                getLog().debug(key + "=" + value);
                message.addHeader(key, value);
            }
        }
    }

    public static DefaultHttpClient wrapClient(DefaultHttpClient base) {
        try {
            SSLContext ctx = SSLContext.getInstance("TLS");
            X509TrustManager tm = new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };
            ctx.init(null, new TrustManager[]{tm}, null);
            SSLSocketFactory ssf = new SSLSocketFactory(ctx);
            ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            ClientConnectionManager ccm = base.getConnectionManager();
            SchemeRegistry sr = ccm.getSchemeRegistry();
            sr.register(new Scheme("https", ssf, 443));
            return new DefaultHttpClient(ccm, base.getParams());
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
