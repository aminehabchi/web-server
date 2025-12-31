package com.http.application.ResourceResolver;

import com.http.config.configFile.ConfigRoute;
import com.http.config.enums.HttpStatus;
import com.http.http.request.HttpRequest;
import com.http.http.response.HttpResponse;
import com.http.utils.Utils;

public class StaticResourceResolver {

    private static final String SOURCE = "./src/main/resources";

    public static boolean resolveRoute(HttpRequest request, HttpResponse response, ConfigRoute route) {
        if (route == null || route.getRoot() == null) {
            return true; // Nothing to resolve
        }

        String requestPath = request.getUrl().getPath();
        String routeBase = route.getPath();
        String fullRootPath = SOURCE + route.getRoot();

        // Exact match with route base
        if (requestPath.equals(routeBase)) {
            return handleBaseRoute(response, route, fullRootPath, routeBase, request);
        }

        // Handle sub-files under the route
        return handleFileRoute(response, route, fullRootPath, routeBase, requestPath);
    }

    private static boolean handleBaseRoute(HttpResponse response, ConfigRoute route, String fullRootPath,
                                           String routeBase, HttpRequest request) {
        if (route.isDirectoryListing()) {
            Utils.serveDirectoryListing(request, response, fullRootPath, routeBase);
        } else if (route.getIndexFile() != null) {
            String indexPath = fullRootPath + "/" + route.getIndexFile();
            Utils.serveFile(response, indexPath, fullRootPath);
        } else {
            response.setStatus(HttpStatus.FORBIDDEN);
            response.setBody("You are not allowed to list this directory".getBytes());
        }
        return false;
    }

    private static boolean handleFileRoute(HttpResponse response, ConfigRoute route, String fullRootPath,
                                           String routeBase, String requestPath) {
        String relativePath = requestPath.substring(routeBase.length());
        String filePath = fullRootPath + relativePath;

        boolean served = Utils.serveFile(response, filePath, fullRootPath);

        if (!served) {
            if (route.getIndexFile() != null) {
                String indexPath = fullRootPath + "/" + route.getIndexFile();
                Utils.serveFile(response, indexPath, fullRootPath);
            } else {
                response.setStatus(HttpStatus.NOT_FOUND);
                response.setBody(("File " + relativePath + " : Not found").getBytes());
            }
        }

        return false;
    }
}
