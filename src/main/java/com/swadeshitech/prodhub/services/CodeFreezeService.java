package com.swadeshitech.prodhub.services;

import com.swadeshitech.prodhub.dto.CodeFreezeRequest;
import com.swadeshitech.prodhub.dto.CodeFreezeResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface CodeFreezeService {
    CodeFreezeResponse createCodeFreeze(CodeFreezeRequest request);
    List<CodeFreezeResponse> getCodeFreezeList();
    CodeFreezeResponse codeFreezeDetails(String id);
    void updateCodeFreeze(String id, CodeFreezeRequest request);
}
