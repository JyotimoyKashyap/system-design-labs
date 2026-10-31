package com.jyotimoykashyap.output;

import com.jyotimoykashyap.LogMessage;

public interface LogAppender {
    void appendLog(LogMessage log);
    void close();
}
