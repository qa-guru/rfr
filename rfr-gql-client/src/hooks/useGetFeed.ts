import {gql} from "@apollo/client";
import {useQuery} from "@apollo/client/react";
import {Connection} from "../types/Connection";
import {Photo} from "../types/Photo";
import {Stat} from "../types/Stat";

type GetFeedData = {
    feed: {
        photos: Connection<Photo>;
        stat: Stat[];
    };
};

export const GET_FEED = gql(`
    query GetFeed($page: Int, $size: Int, $withFriends: Boolean!) {
        feed(withFriends: $withFriends) {
            photos(page: $page, size: $size) {
                edges {
                    node {
                         id
                        src
                        country {
                            code
                            name
                            flag
                        }
                        description
                        isOwner
                        likes {
                            total
                            likes {
                                user 
                            }
                        }
                    }
                }
                pageInfo {
                    hasPreviousPage
                    hasNextPage
                }        
            }
             stat {
                count
                country {
                    code
                }
            }    
        }
    }
`);

type getFeedRequestType = {
    page: number,
    withFriends: boolean,
}
export const useGetFeed = (req: getFeedRequestType) => {
    const {data, loading, error, refetch, fetchMore} = useQuery<GetFeedData>(GET_FEED, {
        variables: {
            withFriends: req.withFriends,
            page: req.page ?? 0,
            size: 12,
        },
        fetchPolicy: "cache-and-network",
    });
    return {
        photos: data?.feed?.photos?.edges?.map((e) => e.node) ?? [],
        stat: data?.feed?.stat ?? [],
        hasPreviousPage: data?.feed?.photos?.pageInfo?.hasPreviousPage ?? false,
        hasNextPage: data?.feed?.photos?.pageInfo?.hasNextPage ?? false,
        loading,
        error,
        refetch,
        fetchMore,
    };
}