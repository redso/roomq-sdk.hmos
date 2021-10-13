package com.example.roomq_module;

import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;
import ohos.agp.components.webengine.ResourceRequest;
import ohos.agp.components.webengine.WebAgent;
import ohos.agp.components.webengine.WebView;
import ohos.app.Context;
import ohos.utils.net.Uri;

public class WaitingRoomAbility extends Ability {
    private WebView webView;
    private Context mContext;
    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_waiting_room);

        mContext = this;
        String url = intent.getStringParam("URL");
        webView = (WebView) findComponentById(ResourceTable.Id_webview);
        webView.setWebAgent(new WebAgent() {
            @Override
            public boolean isNeedLoadUrl(WebView webView, ResourceRequest request) {
                String requestURL = request.getRequestUrl().toString();
                if (requestURL != null && requestURL.startsWith("https://app.noq.com.hk")) {
                    String t = Uri.parse(requestURL).getFirstQueryParamByKey("t");
                    Token.set(mContext,  t != null ? t : "");
                    Intent resultIntent = new Intent();
                    resultIntent.setParam(RoomQ.TOKEN, Token.get(mContext));
                    setResult(0, resultIntent);
                    terminateAbility();
                    return true;
                }
                return super.isNeedLoadUrl(webView, request);
            }
        });
        webView.getWebConfig().setJavaScriptPermit(true);
        webView.getWebConfig().setWebStoragePermit(true);
        webView.load(url);
    }
}
