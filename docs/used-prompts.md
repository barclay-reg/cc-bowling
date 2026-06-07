# Used Prompts

## 1. Initial UI Concept
I roughly think about a ui, that helps to enter the result of the current roll of one bowling player plus displaying an overview of the current game statistics that show: frame, rolls, knocked pins, applies bonus points strike indication, space indication, overall score.

## 2. REST API and OpenAPI Specification Design
design an rest-api, that fulfills the defined stories, write down a open-api spec, that can be later used for code-generation. store it within new folder api-spec. add this prompt to the used-prompts.md file

## 2a. Amendment to REST API and OpenAPI Specification Design
revise the api-design: remove need to versioning of API, we will a decide later which way (url-based, or header-based) we use for this. revise resource model for GAME - status and isCompleted are redundant. add to used prompts and make clear, that this is an amendment to the prompt about api design - write down the used-prompts literally

## 2b. Rediscussing the API Design

rethink the api_design and its resources (also also teh openapi.yaml): GameResponse and FrameResponse and ScoreboardResponse. Why is ScoreboardResponse necessary - why not instead reusing the GameResponse? why does GameResponse contains a Frame, instead of FrameResponse?
lets discuss the approaches to find the best idea

## 2.c Discussing API Design

explain to me the idea behind "isPending" in the FrameResponse?

please rename the fields in FrameResponse

isComplete to rollsComplete
isPending to scorePending
isStrike to strike
isSpare to spare

within GameResponse, change isComplete to gameComplete

Keep suffixed Request, but remove suffixes Response


