/*
 * Copyright (c) 2024 - The MegaMek Team. All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 */

package megamek.ai.utility;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import megamek.client.bot.queen.ai.utility.tw.decision.TWDecisionScoreEvaluator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static megamek.codeUtilities.MathUtility.clamp01;


@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = TWDecisionScoreEvaluator.class, name = "TWDecisionScoreEvaluator"),
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class DecisionScoreEvaluator<IN_GAME_OBJECT, TARGETABLE> implements NamedObject {
    private String name;
    private String description;
    private String notes;
    private final List<Consideration<IN_GAME_OBJECT, TARGETABLE>> considerations = new ArrayList<>();

    public DecisionScoreEvaluator() {
        // no-args constructor for Jackson
    }

    public DecisionScoreEvaluator(String name, String description, String notes) {
        this(name, description, notes, Collections.emptyList());
    }

    public DecisionScoreEvaluator(String name, String description, String notes, List<Consideration<IN_GAME_OBJECT, TARGETABLE>> considerations) {
        this.name = name;
        this.description = description;
        this.notes = notes;
        this.considerations.addAll(considerations);
    }

    public double score(DecisionContext<IN_GAME_OBJECT, TARGETABLE> context, double bonus, double min, DebugReporter debugReport) {
        var finalScore = bonus;
        var considerationSize = getConsiderations().size();
        debugReport.append("Consideration size: " + considerationSize);
        debugReport.append("Bonus: " + bonus);
        for (var consideration : getConsiderations()) {
            if ((0.0f < finalScore) || (0.0f < min)) {
                debugReport.append("Final score is greater than 0.0f or min is greater than 0.0f");
                break;
            }
            var score = consideration.score(context);
            debugReport.append("Consideration: " + consideration.getName());
            debugReport.append("Score: " + score);
            var response = consideration.computeResponseCurve(score);
            response = clamp01(response);
            debugReport.append("curve: " + response);
            finalScore *= clamp01(response);
            debugReport.append("currentScore: " + finalScore);
        }
        // adjustment
        var modificationFactor = 1 - (1 / considerationSize);
        var makeUpValue = (1 - finalScore) * modificationFactor;
        finalScore = finalScore + (makeUpValue * finalScore);
        debugReport.append("Adjusted Final score: " + finalScore);
        return finalScore;
    }


    public List<Consideration<IN_GAME_OBJECT, TARGETABLE>> getConsiderations() {
        return considerations;
    }

    public void addConsideration(Consideration<IN_GAME_OBJECT, TARGETABLE> consideration) {
        considerations.add(consideration);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
