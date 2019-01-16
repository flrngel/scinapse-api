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
import org.elasticsearch.search.aggregations.bucket.filter.*;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.range.Range;
import org.elasticsearch.search.aggregations.bucket.range.RangeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.range.RangeAggregator;
import org.elasticsearch.search.aggregations.bucket.sampler.Sampler;
import org.elasticsearch.search.aggregations.bucket.sampler.SamplerAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
    private static final String YEAR_AGG_NAME = "year";
    private static final String IF_AGG_NAME = "if";
    private static final String IF_10_AGG_NAME = "if10";

    public FilterAggregationBuilder generateYearAggregation(Query query) {
        double year = (double) LocalDate.now().getYear();

        RangeAggregationBuilder yearAgg = AggregationBuilders.range(YEAR_AGG_NAME)
                .field("year")
                .addRange(year - 10, year + 1)
                .subAggregation(AggregationBuilders.histogram(YEAR_AGG_NAME)
                        .field("year")
                        .interval(1)
                        .minDocCount(0)
                        .extendedBounds(year - 10, year));

        // apply filters except year filter
        return AggregationBuilders
                .filter(YEAR_AGG_NAME, query.getFilter().toYearAggFilter())
                .subAggregation(yearAgg);
    }

    public FilterAggregationBuilder generateIfAggregation(Query query) {
        RangeAggregationBuilder ifAgg = AggregationBuilders.range(IF_AGG_NAME)
                .field("journal.impact_factor")
                .addRange(0, 10)
                .subAggregation(AggregationBuilders.histogram(IF_AGG_NAME)
                        .field("journal.impact_factor")
                        .interval(1)
                        .minDocCount(0)
                        .extendedBounds(0, 9));
        RangeAggregationBuilder if10Agg = AggregationBuilders.range(IF_10_AGG_NAME)
                .field("journal.impact_factor")
                .addRange(new RangeAggregator.Range(IF_10_AGG_NAME, 10d, null));

        // apply filters except if filter
        return AggregationBuilders
                .filter(IF_AGG_NAME, query.getFilter().toImpactFactorAggFilter())
                .subAggregation(ifAgg)
                .subAggregation(if10Agg);
    }

    public SamplerAggregationBuilder generateSampleAggregation() {
        TermsAggregationBuilder journalAgg = AggregationBuilders.terms(JOURNAL_AGG_NAME).field("journal.id").size(10);
        TermsAggregationBuilder fosAgg = AggregationBuilders.terms(FOS_AGG_NAME).field("fos.id").size(10);

        // add aggregations using top 100 results
        return AggregationBuilders.sampler(SAMPLE_AGG_NAME)
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
        List<AggregationDto.Year> years = getYears(aggregationMap);
        List<AggregationDto.ImpactFactor> impactFactors = getImpactFactors(aggregationMap);

        // sampler aggregation for top 100 results
        Sampler sample = (Sampler) aggregationMap.get(SAMPLE_AGG_NAME);
        Map<String, Aggregation> samplerMap = sample.getAggregations().getAsMap();

        List<AggregationDto.Journal> journals = getJournals(samplerMap);
        List<AggregationDto.Fos> fosList = getFosList(samplerMap);

        AggregationDto dto = new AggregationDto();
        dto.years = years;
        dto.impactFactors = impactFactors;
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

    private List<AggregationDto.Year> getYears(Map<String, Aggregation> aggregationMap) {
        Filter yearFiltered = (Filter) aggregationMap.get(YEAR_AGG_NAME);
        Range year = yearFiltered.getAggregations().get(YEAR_AGG_NAME);
        List<? extends Range.Bucket> buckets = year.getBuckets();
        Histogram yearHistogram = buckets.get(0).getAggregations().get(YEAR_AGG_NAME);

        List<AggregationDto.Year> years = yearHistogram.getBuckets()
                .stream()
                .map(y -> {
                    AggregationDto.Year yearDto = new AggregationDto.Year();
                    yearDto.year = ((Double) y.getKey()).intValue();
                    yearDto.docCount = y.getDocCount();
                    return yearDto;
                })
                .collect(Collectors.toList());

        AggregationDto.Year allYear = new AggregationDto.Year();
        allYear.year = AggregationDto.ALL;
        allYear.docCount = yearFiltered.getDocCount();
        years.add(allYear);

        return years;
    }

    private List<AggregationDto.ImpactFactor> getImpactFactors(Map<String, Aggregation> aggregationMap) {
        Filter ifFiltered = (Filter) aggregationMap.get(IF_AGG_NAME);

        Range impactFactor = ifFiltered.getAggregations().get(IF_AGG_NAME);
        List<? extends Range.Bucket> buckets = impactFactor.getBuckets();
        Histogram ifHistogram = buckets.get(0).getAggregations().get(IF_AGG_NAME);
        List<AggregationDto.ImpactFactor> impactFactors = ifHistogram.getBuckets()
                .stream()
                .map(y -> {
                    AggregationDto.ImpactFactor ifDto = new AggregationDto.ImpactFactor();
                    int from = ((Double) y.getKey()).intValue();
                    ifDto.from = from;
                    ifDto.to = from + 1;
                    ifDto.docCount = y.getDocCount();
                    return ifDto;
                })
                .collect(Collectors.toList());

        Range impactFactor10 = ifFiltered.getAggregations().get(IF_10_AGG_NAME);
        List<? extends Range.Bucket> if10Buckets = impactFactor10.getBuckets();
        Range.Bucket if10 = if10Buckets.get(0);
        AggregationDto.ImpactFactor if10Dto = new AggregationDto.ImpactFactor();
        if10Dto.from = 10;
        if10Dto.docCount = if10.getDocCount();
        impactFactors.add(if10Dto);

        AggregationDto.ImpactFactor allIf = new AggregationDto.ImpactFactor();
        allIf.from = AggregationDto.ALL;
        allIf.to = AggregationDto.ALL;
        allIf.docCount = ifFiltered.getDocCount();
        impactFactors.add(allIf);

        return impactFactors;
    }

    private List<AggregationDto.Journal> getJournals(Map<String, Aggregation> aggregationMap) {
        Terms journal = (Terms) aggregationMap.get(JOURNAL_AGG_NAME);
        List<Long> journalIds = journal.getBuckets()
                .stream()
                .map(j -> (long) j.getKey())
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
                .map(f -> (long) f.getKey())
                .collect(Collectors.toList());
        return fieldsOfStudyRepository.findByIdIn(fosIds)
                .stream()
                .map(f -> {
                    AggregationDto.Fos fosDto = new AggregationDto.Fos();
                    fosDto.id = f.getId();
                    fosDto.name = f.getName();
                    return fosDto;
                })
                .collect(Collectors.toList());
    }

}
