package main.service;

import lombok.AllArgsConstructor;
import main.api.dto.TagDTO;
import main.api.response.TagResponse;
import main.exception.IllegalParameterException;
import main.model.Tag;
import main.model.repositories.PostRepository;
import main.model.repositories.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class TagService {

    private static final String ERROR_MSG_NORMALIZED_COEFFICIENT = "The value of the normalized coefficient is 0";

    @Autowired
    private final TagRepository tagRepository;
    @Autowired
    private final PostRepository postRepository;

    public TagResponse getTags(String query) {
        int postCount = getCountAcceptedPost();
        if (tagRepository.count() == 0 || postCount == 0) {
            new TagResponse(new ArrayList<>());
        }

        Iterable<Tag> tags = tagRepository.findAll();
        Map<String, Double> tagWeightsNoNormalize = new HashMap<>();
        for (Tag tag : tags) {
            if (!query.isEmpty()) {
                if (!tagIsContainString(tag, query)) {
                    continue;
                }
            }
            tagWeightsNoNormalize.put(tag.getName(), (double) tag.getPosts().size() / postCount);
        }

        double normalizedCoefficient = 1 / tagWeightsNoNormalize.values()
                .stream().max(Comparator.naturalOrder()).orElse(0.0);
        if (normalizedCoefficient == 0.0) {
            throw new IllegalParameterException(ERROR_MSG_NORMALIZED_COEFFICIENT);
        }
        Map<String, Double> tagWeightsNormalize = tagWeightsNoNormalize.entrySet()
                .stream().collect(Collectors.toMap(Map.Entry::getKey, v -> v.getValue() * normalizedCoefficient));

        return new TagResponse(tagWeightsToTagDTO(tagWeightsNormalize));
    }

    private boolean tagIsContainString(Tag tag, String str) {
        if (tag.getName().length() < str.length()) {
            return false;
        }
        return str.equals(tag.getName().substring(0, str.length()));
    }

    private int getCountAcceptedPost() {
        return postRepository.countAllActiveAndAccepted().orElse(0);
    }

    private List<TagDTO> tagWeightsToTagDTO(Map<String, Double> tagWeightsNormalize) {
        List<TagDTO> result = new ArrayList<>();
        tagWeightsNormalize.forEach((k, v) -> result.add(new TagDTO(k, v)));
        return result;
    }
}