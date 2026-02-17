package io.student.rangiffler.jupiter.extension;

import io.student.rangiffler.data.tpl.Connections;

public class DatabasesExtension implements SuiteExtension {

    @Override
    public void afterSuite() {
        Connections.closeAllConnections();
    }
}
