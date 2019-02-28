package io.scinapse.api.service;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import io.scinapse.api.data.academic.repository.FieldsOfStudyRepository;
import io.scinapse.api.data.academic.repository.JournalRepository;
import io.scinapse.api.dto.AggregationDto;
import io.scinapse.api.util.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.filter.*;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.histogram.HistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.sampler.Sampler;
import org.elasticsearch.search.aggregations.bucket.sampler.SamplerAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@XRayEnabled
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchAggregationService {

    private final JournalRepository journalRepository;
    private final FieldsOfStudyRepository fieldsOfStudyRepository;

    private static final String SAMPLE_AGG_NAME = "sample";
    private static final String JOURNAL_AGG_NAME = "journal";
    private static final String FOS_AGG_NAME = "fos";

    private static final String YEAR_ALL_AGG_NAME = "year_all";
    private static final String YEAR_FILTERED_AGG_NAME = "year_filtered";

    public HistogramAggregationBuilder generateYearAllAggregation() {
        return AggregationBuilders.histogram(YEAR_ALL_AGG_NAME)
                .field("year")
                .interval(1)
                .minDocCount(1);
    }

    public FilterAggregationBuilder generateYearFilteredAggregation(Query query) {
        HistogramAggregationBuilder yearAggs = AggregationBuilders.histogram(YEAR_FILTERED_AGG_NAME)
                .field("year")
                .interval(1)
                .minDocCount(1);

        return AggregationBuilders.filter(YEAR_FILTERED_AGG_NAME, query.getFilter().toYearAggFilter())
                .subAggregation(yearAggs);
    }

    public SamplerAggregationBuilder generateSampleAggregation() {
        TermsAggregationBuilder journalAgg = AggregationBuilders.terms(JOURNAL_AGG_NAME).field("journal.id").size(10);
        TermsAggregationBuilder fosAgg = AggregationBuilders.terms(FOS_AGG_NAME).field("fos.id").size(10);

        // add aggregations using top 10 results
        return AggregationBuilders.sampler(SAMPLE_AGG_NAME)
                .shardSize(10)
                .subAggregation(journalAgg)
                .subAggregation(fosAgg);
    }

    // for calculate doc count for each buckets
    public SearchSourceBuilder enhanceAggregationQuery(AggregationDto dto, Query query) {
        FiltersAggregator.KeyedFilter[] journalFilters = dto.journals
                .stream()
                .map(j -> {
                    TermQueryBuilder journalTermQuery = QueryBuilders.termQuery("journal.id", j.id);
                    return new FiltersAggregator.KeyedFilter(String.valueOf(j.id), journalTermQuery);
                })
                .toArray(FiltersAggregator.KeyedFilter[]::new);
        FiltersAggregationBuilder journalAgg = AggregationBuilders.filters(JOURNAL_AGG_NAME, journalFilters);
        FilterAggregationBuilder journalAggFiltered = AggregationBuilders
                .filter(JOURNAL_AGG_NAME, query.getFilter().toJournalAggFilter())
                .subAggregation(journalAgg);

        FiltersAggregator.KeyedFilter[] fosFilters = dto.fosList
                .stream()
                .map(f -> {
                    TermQueryBuilder fosTermQuery = QueryBuilders.termQuery("fos.id", f.id);
                    return new FiltersAggregator.KeyedFilter(String.valueOf(f.id), fosTermQuery);
                })
                .toArray(FiltersAggregator.KeyedFilter[]::new);
        FiltersAggregationBuilder fosAgg = AggregationBuilders.filters(FOS_AGG_NAME, fosFilters);
        FilterAggregationBuilder fosAggFiltered = AggregationBuilders
                .filter(FOS_AGG_NAME, query.getFilter().toFosAggFilter())
                .subAggregation(fosAgg);

        return SearchSourceBuilder.searchSource()
                .query(query.getMainQueryClause())
                .fetchSource(false)
                .size(0)

                // for bucket doc count
                .aggregation(journalAggFiltered)
                .aggregation(fosAggFiltered);
    }

    public AggregationDto convertAggregation(Aggregations aggregations) {
        Map<String, Aggregation> aggregationMap = aggregations.getAsMap();
        List<AggregationDto.Year> yearAll = getYearAll(aggregationMap);
        List<AggregationDto.Year> yearFiltered = getYearFiltered(aggregationMap);

        // sampler aggregation for top 100 results
        Sampler sample = (Sampler) aggregationMap.get(SAMPLE_AGG_NAME);
        Map<String, Aggregation> samplerMap = sample.getAggregations().getAsMap();

        List<AggregationDto.Journal> journals = getJournals(samplerMap);
        List<AggregationDto.Fos> fosList = getFosList(samplerMap);

        AggregationDto dto = new AggregationDto();
        dto.yearAll = yearAll;
        dto.yearFiltered = yearFiltered;

        dto.journals = journals;
        dto.fosList = fosList;

        return dto;
    }

    public void enhanceAggregation(AggregationDto dto, Aggregations aggregations) {
        Map<String, Aggregation> aggregationMap = aggregations.getAsMap();

        Filter journalFiltered = (Filter) aggregationMap.get(JOURNAL_AGG_NAME);
        Filters journal = journalFiltered.getAggregations().get(JOURNAL_AGG_NAME);

        Filter fosFiltered = (Filter) aggregationMap.get(FOS_AGG_NAME);
        Filters fos = fosFiltered.getAggregations().get(FOS_AGG_NAME);

        dto.journals.forEach(j -> {
            Filters.Bucket bucket = journal.getBucketByKey(String.valueOf(j.id));
            if (bucket == null) {
                return;
            }
            j.docCount = bucket.getDocCount();
        });
        dto.journals.sort(Comparator.comparing(j -> j.impactFactor, Comparator.nullsLast(Comparator.reverseOrder())));

        dto.fosList.forEach(f -> {
            Filters.Bucket bucket = fos.getBucketByKey(String.valueOf(f.id));
            if (bucket == null) {
                return;
            }
            f.docCount = bucket.getDocCount();
        });
        dto.fosList.sort(Comparator.comparing(f -> f.docCount, Comparator.reverseOrder()));
    }

    private List<AggregationDto.Year> getYearAll(Map<String, Aggregation> aggregationMap) {
        Histogram histogram = (Histogram) aggregationMap.get(YEAR_ALL_AGG_NAME);

        return histogram.getBuckets()
                .stream()
                .map(y -> {
                    AggregationDto.Year yearDto = new AggregationDto.Year();
                    yearDto.year = ((Double) y.getKey()).intValue();
                    yearDto.docCount = y.getDocCount();
                    return yearDto;
                })
                .collect(Collectors.toList());
    }

    private List<AggregationDto.Year> getYearFiltered(Map<String, Aggregation> aggregationMap) {
        Aggregation aggregation = aggregationMap.get(YEAR_FILTERED_AGG_NAME);
        if (aggregation == null) {
            return null;
        }

        Filter yearFiltered = (Filter) aggregation;
        Histogram yearHistogram = yearFiltered.getAggregations().get(YEAR_FILTERED_AGG_NAME);

        return yearHistogram.getBuckets()
                .stream()
                .map(y -> {
                    AggregationDto.Year yearDto = new AggregationDto.Year();
                    yearDto.year = ((Double) y.getKey()).intValue();
                    yearDto.docCount = y.getDocCount();
                    return yearDto;
                })
                .collect(Collectors.toList());
    }

    private List<AggregationDto.Journal> getJournals(Map<String, Aggregation> aggregationMap) {
        Terms journal = (Terms) aggregationMap.get(JOURNAL_AGG_NAME);
        List<Long> journalIds = journal.getBuckets()
                .stream()
                .map(MultiBucketsAggregation.Bucket::getKeyAsString)
                .map(Long::parseLong)
                .collect(Collectors.toList());

        return journalRepository.findByIdIn(journalIds)
                .stream()
                .map(j -> {
                    AggregationDto.Journal journalDto = new AggregationDto.Journal();
                    journalDto.id = j.getId();
                    journalDto.title = j.getTitle();
                    journalDto.impactFactor = j.getImpactFactor();
                    return journalDto;
                })
                .collect(Collectors.toList());
    }

    private List<AggregationDto.Fos> getFosList(Map<String, Aggregation> aggregationMap) {
        Terms fos = (Terms) aggregationMap.get(FOS_AGG_NAME);
        List<Long> fosIds = fos.getBuckets()
                .stream()
                .map(MultiBucketsAggregation.Bucket::getKeyAsString)
                .map(Long::parseLong)
                .collect(Collectors.toList());

        return fieldsOfStudyRepository.findByIdIn(fosIds)
                .stream()
                .map(f -> {
                    AggregationDto.Fos fosDto = new AggregationDto.Fos();
                    fosDto.id = f.getId();
                    fosDto.name = f.getName();
                    fosDto.level = f.getLevel();
                    return fosDto;
                })
                .collect(Collectors.toList());
    }

}
