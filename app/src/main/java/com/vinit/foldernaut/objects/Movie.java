package com.vinit.foldernaut.objects;

public class Movie {

    String searchUrl, title, rating, description, genre,
            credits, additionalInfo, thumbUrl, posterUrl;

    public Movie(String searchUrl, String title, String rating, String description,
                 String genre, String credits, String additionalInfo,
                 String thumbUrl, String posterUrl) {
        this.searchUrl = searchUrl;
        this.title = title;
        this.rating = rating;
        this.description = description;
        this.genre = genre;
        this.credits = credits;
        this.additionalInfo = additionalInfo;
        this.thumbUrl = thumbUrl;
        this.posterUrl = posterUrl;
    }

    public String getSearchUrl() {
        return searchUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getRating() {
        return rating;
    }

    public String getDescription() {
        return description;
    }

    public String getGenre() {
        return genre;
    }

    public String getCredits() {
        return credits;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public String getThumbUrl() {
        return thumbUrl;
    }

    public String getPosterUrl() {
        return posterUrl;
    }
}
