/*(c) Copyright 2008, VersionOne, Inc. All rights reserved. (c)*/
package com.versionone.integration.common;

import com.versionone.om.V1Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

public interface IConfig {
    @NotNull
    String getUrl();

    @Nullable
    String getUserName();

    @Nullable
    String getPassword();

    @Nullable
    Pattern getPatternObj();

    @Nullable
    String getReferenceField();

    boolean isConnectionValid();

    V1Instance getV1Instance();
}
