xquery version "1.0";

(:~
    Handles the actual display of the search result. The pagination jQuery plugin in jquery-utils.js
    will call this query to retrieve the next page of search results.
    
    The query returns a simple table with four columns: 
    1) the number of the current record, 
    2) a link to save the current record in "My Lists", 
    3) the type of resource (represented by an icon), and 
    4) the data to display.
:)

import module namespace mods="http://www.loc.gov/mods/v3" at "retrieve-mods.xql";
import module namespace jquery="http://exist-db.org/xquery/jquery" at "resource:org/exist/xquery/lib/jquery.xql";
import module namespace security="http://exist-db.org/mods/security" at "security.xqm";
import module namespace sharing="http://exist-db.org/mods/sharing" at "sharing.xqm";
import module namespace clean="http:/exist-db.org/xquery/mods/cleanup" at "cleanup.xql";

declare namespace bs="http://exist-db.org/xquery/biblio/session";

declare option exist:serialize "media-type=application/xhtml+xml";

declare variable $bs:USER := security:get-user-credential-from-session()[1];

declare function bs:collection-is-writable($collection as xs:string) {
    if ($collection eq $sharing:groups-collection) then
        false()
    else
        security:can-write-collection($bs:USER, $collection)
};

declare function bs:retrieve($start as xs:int, $count as xs:int) {
    let $cached := session:get-attribute("mods:cached")
    let $stored := session:get-attribute("mods-personal-list")
    let $total := count($cached)
    let $available :=
        if ($start + $count gt $total) then
            $total - $start + 1
        else
            $count
    return
        <table xmlns="http://www.w3.org/1999/xhtml">
        {
            for $item at $pos in subsequence($cached, $start, $available)
            let $currentPos := $start + $pos - 1
            let $id := concat(document-uri(root($item)), '#', util:node-id($item))
            let $saved := $stored//*[@id = $id]
            return
                <tr>
                    <td class="current">{$currentPos}</td>
                    {
                        if ($count gt 1) then
                            <td class="actions-cell">
                                <a id="save_{$id}" href="#{$currentPos}" class="save">
                                    <img title="save to my list" 
                                        src="../../../resources/images/{if ($saved) then 'disk_gew.gif' else 'disk.gif'}"
                                        class="{if ($saved) then 'stored' else ''}"/>
                                </a>
                            </td>
                        else
                            ()
                    }
                    <td>
                        <img title="{$item/mods:typeOfResource/string()}" 
                          src="../../../resources/images/{mods:return-type(string($currentPos), $item)}.png"/>
                    </td>
                    {
                        if ($count eq 1) then
                            <td class="detail-view">
                                <div class="actions-toolbar">
                                    <a id="save_{$id}" href="#{$currentPos}" class="save">
                                       <img title="save to my list" 
                                           src="../../../resources/images/{if ($saved) then 'disk_gew.gif' else 'disk.gif'}"
                                           class="{if ($saved) then 'stored' else ''}"/>
                                    </a>
                                    {
                                        if (bs:collection-is-writable(util:collection-name($item))) then (
                                            <a href="../edit/edit.xq?id={$item/@ID}&amp;collection={util:collection-name($item)}">
                                                <img title="edit" src="../../../resources/images/page_edit.png"/>
                                            </a>,
                                            (: <a class="add-related" href="../edit/edit.xq?type=default&amp;collection={util:collection-name($item)}&amp;host={$item/@ID}">:)
                                            <a class="add-related" href="#{util:collection-name($item)}#{$item/@ID}">
                                                <img title="add related item" src="../../../resources/images/page_add.png"/>
                                            </a>,
                                           <a class="remove-resource" href="#{$id}"><img title="delete" src="../../../resources/images/delete.png"/></a>,
                                           <a class="move-resource" href="#{$id}"><img title="move" src="../../../resources/images/shape_move_front.png"/></a>
                                        ) else
                                            ()
                                    }
                                </div>
                                {
                                    let $clean := clean:cleanup($item)
                                    let $log := util:log("DEBUG", ("RECORD: ", $clean))
                                    return
                                        mods:format-full(string($currentPos), $clean) 
                                }
                            </td>
                        else
                            <td class="pagination-toggle"><a>{mods:format-short(string($currentPos), $item)}</a></td>
                    }
                </tr>
        }
        </table>
};

session:create(),
let $start0 := request:get-parameter("start", ())
let $start := xs:int(if ($start0) then $start0 else 1)
let $count0 := request:get-parameter("count", ())
let $count := xs:int(if ($count0) then $count0 else 10)
return
    bs:retrieve($start, $count)