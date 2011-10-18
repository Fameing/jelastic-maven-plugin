package org.codehaus.mojo.jelastic.model;

public class CreateObject {
    private JelasticResponse response;
    private int result;
    private String error;
    private Debug debug;

    public CreateObject() {
    }

    public static class JelasticObject {
        private int id;
        private String developer;
        private String uploadDate;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getDeveloper() {
            return developer;
        }

        public void setDeveloper(String developer) {
            this.developer = developer;
        }

        public String getUploadDate() {
            return uploadDate;
        }

        public void setUploadDate(String uploadDate) {
            this.uploadDate = uploadDate;
        }
    }

    public static class JelasticResponse {
        private int id;
        private int result;
        private JelasticObject object;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getResult() {
            return result;
        }

        public void setResult(int result) {
            this.result = result;
        }

        public JelasticObject getObject() {
            return object;
        }

        public void setObject(JelasticObject object) {
            this.object = object;
        }
    }

    public JelasticResponse getResponse() {
        return response;
    }

    public void setResponse(JelasticResponse response) {
        this.response = response;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public Debug getDebug() {
        return debug;
    }

    public void setDebug(Debug debug) {
        this.debug = debug;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
