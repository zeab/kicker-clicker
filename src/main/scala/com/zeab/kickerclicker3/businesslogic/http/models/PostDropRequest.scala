package com.zeab.kickerclicker3.businesslogic.http.models

case class PostDropRequest(
                            name: String,
                            color: String,
                            url: String,
                            imageUrl: String,
                            dateTime: String,
                            isWanted: Boolean
                          )
