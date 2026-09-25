import {ApolloError} from "@apollo/client";

const USER_FACING_CLASSIFICATIONS = ["BAD_REQUEST", "NOT_FOUND"];
const UNAUTHORIZED_CLASSIFICATIONS = ["UNAUTHORIZED", "FORBIDDEN"];

const classifications = (error: ApolloError): unknown[] =>
    error.graphQLErrors.map((e) => e.extensions?.classification);

export const errorMessage = (error: ApolloError | undefined, fallback: string): string => {
    const userFacing = error?.graphQLErrors.find((e) =>
        USER_FACING_CLASSIFICATIONS.includes(String(e.extensions?.classification)));
    return userFacing?.message || fallback;
};

export const isUnauthorized = (error: ApolloError | undefined): boolean => {
    if (!error) {
        return false;
    }
    const networkError = error.networkError as { statusCode?: number } | null;
    return networkError?.statusCode === 401
        || classifications(error).some((c) => UNAUTHORIZED_CLASSIFICATIONS.includes(String(c)));
};
