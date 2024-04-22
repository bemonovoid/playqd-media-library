create view artist_view as

select a.artist_id                  as id,
       a.artist_name                as name,
       a.artist_country             as country,
       a.spotify_artist_id          as spotify_id,
       count(distinct a.album_name) as total_albums,
       count(a.id)                  as total_tracks
from audio_file a
group by a.artist_id, a.artist_name, a.artist_country, a.spotify_artist_id;

create view album_view as

select a.album_id           as id,
       a.album_name         as name,
       a.artist_id          as artist_id,
       a.artist_name        as artist_name,
       a.album_release_date as release_date,
       a.genre_id           as genre_id,
       a.genre              as genre,
       a.artwork_embedded   as artwork_embedded,
       a.spotify_album_id   as spotify_id,
       count(a.id)          as total_tracks
from audio_file a
group by a.artist_id, a.artist_name, a.album_id, a.album_name, a.album_release_date, a.genre_id, a.genre,
         a.artwork_embedded, a.spotify_album_id;

create view genre_view as

select a.genre_id                       as id,
       a.genre                          as name,
       count(distinct a.artist_name)    as total_artists,
       count(distinct a.album_name)     as total_albums,
       count(a.id)                      as total_tracks
from audio_file a
group by a.genre_id, a.genre;

# create table if not exists audio_file
# (
#     id                      bigint auto_increment primary key,
#
#     name                    VARCHAR(255) not null,
#     size                    bigint       not null,
#     location                varchar(255) not null,
#     extension               varchar(255) not null,
#     file_last_scanned_date  datetime(6)  not null,
#     file_last_modified_date datetime(6)  null,
#
#     formats                  varchar(100) null,
#     bit_rate                varchar(100) null,
#     lossless                boolean default false,
#     channels                varchar(100) null,
#     sample_rate             varchar(100) null,
#     encoding_type           varchar(100) null,
#     bits_per_sample         int          null,
#
#     artist_name             varchar(512) null,
#     artist_country          varchar(255) null,
#
#     album_name              varchar(512) null,
#     album_release_date      varchar(255) null,
#
#     track_name              varchar(255) null,
#     track_number            varchar(255) null,
#     track_length            int          null,
#
#     comment                 longtext     null,
#     lyrics                  longtext     null,
#     genre                   varchar(255) null,
#
#     artwork_embedded        boolean default false,
#
#     mb_track_id             varchar(255) null,
#     mb_artist_id            varchar(255) null,
#     mb_release_type         varchar(255) null,
#     mb_release_group_id     varchar(255) null,
#
#     created_by              varchar(255) not null,
#     created_date            datetime(6)  not null,
#     last_modified_by        varchar(255) null,
#     last_modified_date      datetime(6)  null
# );
#
# create index audio_file_idx
#     on audio_file (location);
