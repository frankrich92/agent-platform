package com.htam.agent.common.vo;

import com.htam.agent.common.entity.RagDocumentChunk;
import lombok.Getter;
import lombok.Setter;

/**
 * 描述：RagDocumentChunk VO
 *
 * @author huxuehao
 **/
@Getter
@Setter
public class RagDocumentChunkVO extends RagDocumentChunk {
    private double score = 0;
}
