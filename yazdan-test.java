how can use schedule for getting reconncetion to db
 public void run(ApplicationArguments args) {
        ObjectMapper objectMapper = new ObjectMapper();
        String serviceUri = faceimageAddImageAndIdentityUri;

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(url);
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);
//        hikariConfig.setMaximumPoolSize();
        hikariConfig.setConnectionTimeout(2000);
        hikariConfig.setIdleTimeout(60000);
//        hikariConfig.setMaxLifetime(1800000);
        HikariDataSource dataSource = new HikariDataSource(hikariConfig);

        Locale.setDefault(Locale.US);

        String selectScript = "SELECT t.NATIONAL_CODE, t.GENDER, t.BIRTH_DATE, t.FIRST_NAME, t.LAST_NAME, t.FATHER_NAME" +
                ", t.TAKE_PICTURE_DATE, t.PICTURE_CONTENT, t.PICTURE_ID FROM IDEN.VW_IDEN_FOR_BIOMETRIC t ";
        String updateScript = "UPDATE IDEN.VW_IDEN_FOR_BIOMETRIC_UPDATE t SET t.SENT_TO_ABIS = ? where " +
                "t.PICTURE_ID = ?";

        while (true) {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(selectScript);
                 PreparedStatement updatePreparedStatement = connection.prepareStatement(updateScript)) {
                preparedStatement.setQueryTimeout(10);
                updatePreparedStatement.setQueryTimeout(10);


                preparedStatement.setFetchSize(200); // view return only 200 rows
                while (true) {
//                    System.out.pr
