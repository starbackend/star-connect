package star.connect.imdate.format;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.io.ByteSink;
import com.google.common.io.ByteSource;
import com.google.common.io.InputSupplier;
import org.cwatch.imdate.ImdateProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.HttpMessageConverterExtractor;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;

@Component
public class ImdateRestClient {

    final private RestTemplate restTemplate;

    final private String httpUrl;

    final private String httpUser;

    public ImdateRestClient(RestTemplate restTemplate, String httpUrl, String httpUser) {
        this.restTemplate = restTemplate;
        this.httpUrl = httpUrl;
        this.httpUser = httpUser;
    }

    @Autowired
    public ImdateRestClient(ImdateProperties.Env env) {
        this(new RestTemplate(), env.getHttp().getUrl().toString(), env.getWup().getUser());
    }

    public ImdateRestResponse deleteProjectsOvr(String project) {
        ResponseEntity<ImdateRestResponse> result = restTemplate.getForEntity(
                "{url}/ovrws/deleteProjectOvr?userId={user}&project={project}",
                ImdateRestResponse.class,
                httpUrl,
                httpUser,
                project
        );
        ImdateRestResponse body = result.getBody();
        if ("KO".equals(body.response)) {
            throw new ImdateRestException(body.errorMessage);
        }
        return body;
    }

    public ImdateRestResponse createProject(String project, String description) {
        // Possible responses from imdate:
        // {response: "KO", errorMessage: "Project test already into DB"}
        // {response: "OK"}
        ResponseEntity<ImdateRestResponse> result = restTemplate.getForEntity(
                "{url}/ovrws/createProject?userId={user}&name={name}&description={description}",
                ImdateRestResponse.class,
                httpUrl,
                httpUser,
                project,
                MoreObjects.firstNonNull(Strings.emptyToNull(description), project)
        );
        ImdateRestResponse body = result.getBody();
        if ("KO".equals(body.response)) {
            throw new ImdateRestException(body.errorMessage);
        }
        return body;
    }

    public ImdateRestResponse populateProjectOvr(ByteSource cdf) {
        final RestTemplate restTemplate = new RestTemplate();
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setBufferRequestBody(false);
        restTemplate.setRequestFactory(requestFactory);

        RequestCallback requestCallback = new RequestCallback() {
            @Override
            public void doWithRequest(ClientHttpRequest request) throws IOException {
                cdf.copyTo(request.getBody());
            }
        };

        ImdateRestResponse body = restTemplate.execute(
                "{url}/ovrws/populateProjectOvr?userId={user}",
                HttpMethod.POST,
                requestCallback,
                new HttpMessageConverterExtractor<>(ImdateRestResponse.class, restTemplate.getMessageConverters()),
                httpUrl,
                httpUser
        );
        if ("KO".equals(body.response)) {
            throw new ImdateRestException(body.errorMessage);
        }
        return body;

    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static class ImdateRestResponse {
        public String response;
        public String errorMessage;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class ImdateRestException extends RuntimeException {
        public ImdateRestException(String message) {
            super(message);
        }
    }




}
