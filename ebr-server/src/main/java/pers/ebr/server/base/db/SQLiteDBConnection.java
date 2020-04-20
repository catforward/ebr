/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pers.ebr.server.base.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.ebr.server.base.Paths;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * <pre>
 * The SQLITE Database Storage Implementation
 * </pre>
 *
 * @author l.gong
 */
public class SQLiteDBConnection implements DBConnection {
    final static String TYPE = "sqlite";
    final static String SCHEMA = "ebr.dat";

    private final static Logger logger = LoggerFactory.getLogger(SQLiteDBConnection.class);

    private DBConnectionBuilder builder;
    private Connection connection = null;

    SQLiteDBConnection(DBConnectionBuilder builder) {
        this.builder = builder;
    }

    @Override
    public DBConnection init() {
        String connStr = String.format("jdbc:sqlite:%s%s%s", Paths.getDataPath(), File.separator, SCHEMA);
        try {
            connection = DriverManager.getConnection(connStr);
        } catch (SQLException ex) {
            logger.error("database error occurred...", ex);
            throw new RuntimeException(ex);
        }
        // TODO
        return this;
    }

    @Override
    public void close() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException ex) {
             logger.error("database error occurred...", ex);
        }
    }



}
