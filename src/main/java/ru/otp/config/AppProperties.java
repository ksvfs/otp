package ru.otp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private final Jwt jwt = new Jwt();
    private final Otp otp = new Otp();
    private final FileStorage file = new FileStorage();
    private final Telegram telegram = new Telegram();
    private final Smpp smpp = new Smpp();
    private final Mail mail = new Mail();

    public Jwt getJwt() {
        return jwt;
    }

    public Otp getOtp() {
        return otp;
    }

    public FileStorage getFile() {
        return file;
    }

    public Telegram getTelegram() {
        return telegram;
    }

    public Smpp getSmpp() {
        return smpp;
    }

    public Mail getMail() {
        return mail;
    }

    public static class Jwt {
        private String secret = "111112nQ9rF5vH3jL1wRRRRRRW6aB0cD9gZ3fG7hJ5kLksfk8R1sT6uV9jjlji";
        private long ttlSeconds = 3600;

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public long getTtlSeconds() {
            return ttlSeconds;
        }

        public void setTtlSeconds(long ttlSeconds) {
            this.ttlSeconds = ttlSeconds;
        }
    }

    public static class Otp {
        private long expireCheckIntervalMs = 60000;

        public long getExpireCheckIntervalMs() {
            return expireCheckIntervalMs;
        }

        public void setExpireCheckIntervalMs(long expireCheckIntervalMs) {
            this.expireCheckIntervalMs = expireCheckIntervalMs;
        }
    }

    public static class FileStorage {
        private String path = "otp-codes.log";

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }

    public static class Telegram {
        private String baseUrl = "https://api.telegram.org";
        private String botToken = "";
        private String defaultChatId = "";

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getBotToken() {
            return botToken;
        }

        public void setBotToken(String botToken) {
            this.botToken = botToken;
        }

        public String getDefaultChatId() {
            return defaultChatId;
        }

        public void setDefaultChatId(String defaultChatId) {
            this.defaultChatId = defaultChatId;
        }
    }

    public static class Smpp {
        private String host = "localhost";
        private int port = 2775;
        private String systemId = "smppclient1";
        private String password = "password";
        private String systemType = "OTP";
        private String sourceAddr = "OTPService";

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getSystemId() {
            return systemId;
        }

        public void setSystemId(String systemId) {
            this.systemId = systemId;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getSystemType() {
            return systemType;
        }

        public void setSystemType(String systemType) {
            this.systemType = systemType;
        }

        public String getSourceAddr() {
            return sourceAddr;
        }

        public void setSourceAddr(String sourceAddr) {
            this.sourceAddr = sourceAddr;
        }
    }

    public static class Mail {
        private String from = "no-reply@otp.local";

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }
    }
}
