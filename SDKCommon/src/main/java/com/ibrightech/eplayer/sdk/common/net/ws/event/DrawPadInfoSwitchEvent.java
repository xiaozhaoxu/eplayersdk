package com.ibrightech.eplayer.sdk.common.net.ws.event;


import com.ibrightech.eplayer.sdk.common.entity.SessionData.DrawPadInfo;

/**
 * Created by junhai on 14-8-13.
 */
public class DrawPadInfoSwitchEvent {
    private DrawPadInfo padInfo;

    public DrawPadInfo getPadInfo() {
        return padInfo;
    }

    public void setPadInfo(DrawPadInfo padInfo) {
        this.padInfo = padInfo;
    }
    public DrawPadInfoSwitchEvent(DrawPadInfo padInfo) {
        this.padInfo = padInfo;
    }
}
