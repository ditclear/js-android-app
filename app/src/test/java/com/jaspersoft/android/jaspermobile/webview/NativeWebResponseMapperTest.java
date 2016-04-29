package com.jaspersoft.android.jaspermobile.webview;

import android.annotation.TargetApi;
import android.os.Build;
import android.webkit.WebResourceResponse;

import org.apache.tools.ant.filters.StringInputStream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

/**
 * @author Tom Koptel
 * @since 2.5
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 21)
public class NativeWebResponseMapperTest {
    private static final Map<String, String> HEADERS = new HashMap<>();
    static {
        HEADERS.put("Cookie", "jsessionid=123");
    }

    private InputStream data = new StringInputStream("data");
    private NativeWebResponseMapper nativeWebResponseMapper;

    @Mock
    WebResponse webResponse;
    private WebResourceResponse mappedResponse;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        nativeWebResponseMapper = new NativeWebResponseMapper();
    }

    @Test
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void should_map_lollipop_web_response_to_native_response() throws Exception {
        givenAndroidOfVersion(Build.VERSION_CODES.LOLLIPOP);
        givenWebResponse();

        whenMapsWebResourceResponse();

        assertThat(mappedResponse.getData(), is(mappedResponse.getData()));
        assertThat(mappedResponse.getEncoding(), is(mappedResponse.getEncoding()));
        assertThat(mappedResponse.getMimeType(), is(mappedResponse.getMimeType()));
        assertThat(mappedResponse.getReasonPhrase(), is(mappedResponse.getReasonPhrase()));
        assertThat(mappedResponse.getResponseHeaders(), is(mappedResponse.getResponseHeaders()));
        assertThat(mappedResponse.getStatusCode(), is(mappedResponse.getStatusCode()));
    }

    @Test
    public void should_map_legacy_web_response_to_native_response() throws Exception {
        givenAndroidOfVersion(Build.VERSION_CODES.ICE_CREAM_SANDWICH);
        givenWebResponse();

        whenMapsWebResourceResponse();

        assertThat(mappedResponse.getData(), is(mappedResponse.getData()));
        assertThat(mappedResponse.getEncoding(), is(mappedResponse.getEncoding()));
        assertThat(mappedResponse.getMimeType(), is(mappedResponse.getMimeType()));
    }

    private void whenMapsWebResourceResponse() {
        mappedResponse = nativeWebResponseMapper.toNativeResponse(webResponse);
    }

    private void givenAndroidOfVersion(int version) {
        ReflectionHelpers.setStaticField(Build.VERSION.class, "SDK_INT", version);
    }

    private void givenWebResponse() {
        when(webResponse.getData()).thenReturn(data);
        when(webResponse.getEncoding()).thenReturn("utf-8");
        when(webResponse.getMimeType()).thenReturn("application/json");
        when(webResponse.getReasonPhrase()).thenReturn("OK");
        when(webResponse.getResponseHeaders()).thenReturn(HEADERS);
        when(webResponse.getStatusCode()).thenReturn(200);
    }
}