package com.edgelight.ledconnector;

import java.util.List;

import com.edgelight.common.RGB;

public interface LedConnector {
    public void submit(List<RGB> rgbs);
}
