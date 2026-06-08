package ru.sagenotes.searchservice.data.service

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch.core.SearchResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.sagenotes.searchservice.data.model.ElasticsearchResultDto
import ru.sagenotes.searchservice.data.model.SearchResultDto
import ru.sagenotes.searchservice.domain.model.SearchSource

interface ElasticsearchService {
    suspend fun search(query: String, userId: String, limit: Int = 10): List<SearchResultDto>
}

class ElasticsearchServiceImpl(
    private val elasticsearchClient: ElasticsearchClient
) : ElasticsearchService {
    override suspend fun search(query: String, userId: String, limit: Int): List<SearchResultDto> {
        return withContext(Dispatchers.IO) {
            val userIdFilter = Query.of { queryBuilder ->
                queryBuilder.term { termQueryBuilder ->
                    termQueryBuilder.field("user_id.keyword").value(userId)
                }
            }

            val textMatch = Query.of { queryBuilder ->
                queryBuilder.match { matchQueryBuilder ->
                    matchQueryBuilder.field("text").query(query)
                }
            }

            val boolQuery = Query.of { queryBuilder ->
                queryBuilder.bool { boolQueryBuilder ->
                    boolQueryBuilder.filter(userIdFilter).must(textMatch)
                }
            }

            val response: SearchResponse<ElasticsearchResultDto> = elasticsearchClient.search(
                { search ->
                    search.index("notes")
                        .query(boolQuery)
                        .size(limit)
                        .highlight { highlightBuilder ->
                            highlightBuilder.fields("text") { highlightFieldBuilder ->
                                highlightFieldBuilder
                                    .numberOfFragments(2)
                                    .fragmentSize(50)
                            }
                        }
                }, ElasticsearchResultDto::class.java)

            response.hits().hits().mapNotNull { hit ->
                val sourceMap = hit.source() ?: return@mapNotNull null

                val highlightFragments = hit.highlight()["text"]

                val displayText = if (!highlightFragments.isNullOrEmpty()) {
                    highlightFragments.joinToString(separator = " ... ")
                } else {
                    sourceMap.text
                }

                SearchResultDto(
                    noteId = hit.id() ?: "",
                    text = displayText,
                    score = hit.score() ?: 0.0,
                    source = SearchSource.ELASTICSEARCH
                )
            }
        }
    }
}