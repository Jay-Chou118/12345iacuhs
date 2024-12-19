import{bP as s}from"./index-78ce1aa3.js";/*!
 * normalize-path <https://github.com/jonschlinkert/normalize-path>
 *
 * Copyright (c) 2014-2018, Jon Schlinkert.
 * Released under the MIT License.
 */var t=function(r,f){if(typeof r!="string")throw new TypeError("expected path to be a string");if(r==="\\"||r==="/")return"/";var i=r.length;if(i<=1)return r;var n="";if(i>4&&r[3]==="\\"){var o=r[2];(o==="?"||o===".")&&r.slice(0,2)==="\\\\"&&(r=r.slice(2),n="//")}var e=r.split(/[/\\]+/);return f!==!1&&e[e.length-1]===""&&e.pop(),n+e.join("/")};const g=s(t);export{g as n};
