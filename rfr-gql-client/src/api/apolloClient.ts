import {ApolloClient, createHttpLink, from, InMemoryCache} from "@apollo/client";
import {setContext} from "@apollo/client/link/context";
import {onError} from "@apollo/client/link/error";
import {clearSession, idTokenFromLocalStorage} from "./authUtils";


const API_URL = `${import.meta.env.VITE_API_URL}`;

const apolloHttpLink = createHttpLink({
    uri: `${API_URL}/graphql`,
})

const headerLink = setContext((_request, previousContext) => ({
    headers: {
        ...previousContext.headers,
        "Authorization": idTokenFromLocalStorage() ? `Bearer ${idTokenFromLocalStorage()}` : "",
    },
}));

const errorLink = onError(({graphQLErrors, networkError, operation}) => {
    if ((networkError as { statusCode?: number } | undefined)?.statusCode === 401) {
        clearSession();
        if (window.location.pathname !== "/") {
            window.location.replace("/");
        }
        return;
    }
    if (import.meta.env.DEV) {
        graphQLErrors?.forEach((e) => console.error(`[GraphQL error] ${operation.operationName}:`, e.message, e.extensions));
        if (networkError) {
            console.error(`[Network error] ${operation.operationName}:`, networkError);
        }
    }
});

export const apiClient = new ApolloClient({
    link: from([errorLink, headerLink, apolloHttpLink]),
    cache: new InMemoryCache(),
});