package main.service;

import main.api.response.TagResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class TagService {

    public TagResponse getTags(String query) {

        // TODO: Заглушка

        System.out.println("------------------------------------");
        System.out.println(query);

        return new TagResponse(new ArrayList<>());
    }
}
