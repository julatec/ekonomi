package name.julatec.ekonomi.session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
public class WorkspaceController {

    private WorkspaceService service;

    @RequestMapping(
            path = "/js/session.js",
            method = RequestMethod.GET)
    public void session(
            final Authentication authentication,
            final HttpServletRequest request,
            final HttpServletResponse response) throws IOException {
        final Workspace workspace = service.getWorkspace(authentication, request);
        response.setContentType("application/javascript;charset=utf-8");
        service.printSession(workspace, response.getWriter(), request.getLocale());
    }

    @Autowired
    WorkspaceController setService(WorkspaceService service) {
        this.service = service;
        return this;
    }

}
